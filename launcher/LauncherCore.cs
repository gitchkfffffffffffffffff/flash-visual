using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.IO.Compression;
using System.Net;
using System.Text;
using System.Threading;
using System.Web.Script.Serialization;

namespace FlashVisualLauncher
{
    public class LAccount
    {
        public string Name { get; set; }
        public string Type { get; set; }
    }

    public class LConfig
    {
        public List<LAccount> Accounts = new List<LAccount>();
        public int ActiveAccount = 0;
        public string InstallDir = "";
        public int RamMb = 2048;
        public string JavaPath = "";
        public int Width = 854;
        public int Height = 480;
        public string SelectedVersion = "";
    }

    public static class Core
    {
        public const string MC_VERSION = "1.21.11";
        public const string LOADER = "0.19.3";
        public const string MOD_VERSION = "1.1.0-pre1";

        public static double Progress = 0;

        public static LConfig Config = new LConfig();
        public static string ConfigPath = Path.Combine(AppContext.BaseDirectory, "launcher.json");

        static Core()
        {
            ServicePointManager.SecurityProtocol = (SecurityProtocolType)(192 | 768 | 3072);
        }

        public static void LoadConfig()
        {
            try
            {
                if (!File.Exists(ConfigPath)) return;
                string json = File.ReadAllText(ConfigPath, Encoding.UTF8);
                var src = new JavaScriptSerializer().Deserialize<Dictionary<string, object>>(json);
                if (src == null) return;
                if (src.ContainsKey("InstallDir")) Config.InstallDir = (string)src["InstallDir"];
                if (src.ContainsKey("RamMb")) Config.RamMb = Convert.ToInt32(src["RamMb"]);
                if (src.ContainsKey("JavaPath")) Config.JavaPath = (string)src["JavaPath"];
                if (src.ContainsKey("Width")) Config.Width = Convert.ToInt32(src["Width"]);
                if (src.ContainsKey("Height")) Config.Height = Convert.ToInt32(src["Height"]);
                if (src.ContainsKey("ActiveAccount")) Config.ActiveAccount = Convert.ToInt32(src["ActiveAccount"]);
                if (src.ContainsKey("SelectedVersion")) Config.SelectedVersion = (string)src["SelectedVersion"];
                if (src.ContainsKey("Accounts"))
                {
                    foreach (object o in (object[])src["Accounts"])
                    {
                        var d = (Dictionary<string, object>)o;
                        Config.Accounts.Add(new LAccount
                        {
                            Name = (string)d["Name"],
                            Type = d.ContainsKey("Type") ? (string)d["Type"] : "offline"
                        });
                    }
                }
            }
            catch { }
        }

        public static void SaveConfig()
        {
            try
            {
                var accs = new List<Dictionary<string, object>>();
                foreach (var a in Config.Accounts)
                {
                    accs.Add(new Dictionary<string, object> { { "Name", a.Name }, { "Type", a.Type } });
                }
                var d = new Dictionary<string, object>
                {
                    { "Accounts", accs },
                    { "ActiveAccount", Config.ActiveAccount },
                    { "InstallDir", Config.InstallDir },
                    { "RamMb", Config.RamMb },
                    { "JavaPath", Config.JavaPath },
                    { "Width", Config.Width },
                    { "Height", Config.Height },
                    { "SelectedVersion", Config.SelectedVersion }
                };
                File.WriteAllText(ConfigPath, new JavaScriptSerializer().Serialize(d), Encoding.UTF8);
            }
            catch { }
        }

        public static string InstallDir()
        {
            string d = Config.InstallDir;
            if (string.IsNullOrEmpty(d))
            {
                d = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), ".minecraft");
            }
            try { return Environment.ExpandEnvironmentVariables(d); } catch { return d; }
        }

        public static LAccount Active()
        {
            if (Config.Accounts.Count == 0) return null;
            int i = Config.ActiveAccount;
            if (i < 0 || i >= Config.Accounts.Count) i = 0;
            return Config.Accounts[i];
        }

        public static void SyncAltsToMod()
        {
            try
            {
                string dir = Path.Combine(InstallDir(), "config", "flash-visual");
                Directory.CreateDirectory(dir);
                string path = Path.Combine(dir, "alts.txt");
                var sb = new StringBuilder();
                int idx = Config.ActiveAccount;
                if (Config.Accounts.Count > 0 && idx >= 0 && idx < Config.Accounts.Count)
                {
                    sb.AppendLine("#active=" + Config.Accounts[idx].Name);
                }
                foreach (var a in Config.Accounts)
                {
                    sb.AppendLine(a.Name);
                }
                File.WriteAllText(path, sb.ToString(), Encoding.UTF8);
            }
            catch { }
        }

        static WebClient NewClient()
        {
            var wc = new WebClient();
            wc.Encoding = Encoding.UTF8;
            wc.Headers[HttpRequestHeader.UserAgent] = "FlashVisualLauncher/" + MOD_VERSION;
            return wc;
        }

        static Dictionary<string, object> GetJson(string url)
        {
            using (var wc = NewClient())
            {
                string s = wc.DownloadString(url);
                return (Dictionary<string, object>)new JavaScriptSerializer().DeserializeObject(s);
            }
        }

        static Dictionary<string, object> ParseJson(string json)
        {
            return (Dictionary<string, object>)new JavaScriptSerializer().DeserializeObject(json);
        }

        static object[] GetJsonArray(string url)
        {
            using (var wc = NewClient())
            {
                string s = wc.DownloadString(url);
                return (object[])new JavaScriptSerializer().DeserializeObject(s);
            }
        }

        static void DownloadUrl(string url, string dest)
        {
            string dir = Path.GetDirectoryName(dest);
            if (!Directory.Exists(dir)) Directory.CreateDirectory(dir);
            string tmp = dest + ".tmp";
            using (var wc = NewClient())
            {
                wc.DownloadFile(url, tmp);
            }
            if (File.Exists(dest)) File.Delete(dest);
            File.Move(tmp, dest);
        }

        static string MavenPath(string name)
        {
            string[] p = name.Split(':');
            return p[0].Replace('.', '/') + "/" + p[1] + "/" + p[2] + "/" + p[1] + "-" + p[2] + ".jar";
        }

        static bool OsRuleMatch(Dictionary<string, object> os)
        {
            if (os == null) return true;
            if (os.ContainsKey("name") && !"windows".Equals((string)os["name"], StringComparison.OrdinalIgnoreCase))
                return false;
            return true;
        }

        static bool RuleAllowed(Dictionary<string, object> lib)
        {
            if (!lib.ContainsKey("rules")) return true;
            bool allowed = false;
            foreach (var item in (object[])lib["rules"])
            {
                var r = (Dictionary<string, object>)item;
                string action = (string)r["action"];
                bool match = true;
                if (r.ContainsKey("os")) match = OsRuleMatch((Dictionary<string, object>)r["os"]);
                if (match) allowed = action.Equals("allow", StringComparison.OrdinalIgnoreCase);
            }
            return allowed;
        }

        static void TryDownload(string url, string local)
        {
            try
            {
                if (File.Exists(local) && new FileInfo(local).Length > 0) return;
                DownloadUrl(url, local);
            }
            catch { }
        }

        static void ExtractZip(string zip, string outDir)
        {
            if (!File.Exists(zip)) return;
            if (!Directory.Exists(outDir)) Directory.CreateDirectory(outDir);
            using (var arc = ZipFile.OpenRead(zip))
            {
                foreach (var e in arc.Entries)
                {
                    if (string.IsNullOrEmpty(e.Name)) continue;
                    string dest = Path.Combine(outDir, e.FullName.Replace('/', Path.DirectorySeparatorChar));
                    string dir = Path.GetDirectoryName(dest);
                    if (!Directory.Exists(dir)) Directory.CreateDirectory(dir);
                    if (File.Exists(dest)) continue;
                    e.ExtractToFile(dest, true);
                }
            }
        }

        public static string AutoDetectJava()
        {
            try
            {
                string home = Environment.GetEnvironmentVariable("JAVA_HOME");
                if (!string.IsNullOrEmpty(home))
                {
                    string j = Path.Combine(home, "bin", "java.exe");
                    if (File.Exists(j)) return j;
                }
                using (var key = Microsoft.Win32.Registry.LocalMachine.OpenSubKey(
                    @"SOFTWARE\JavaSoft\Java Runtime Environment"))
                {
                    if (key != null)
                    {
                        string cur = (string)key.GetValue("CurrentVersion");
                        using (var k = key.OpenSubKey(cur ?? ""))
                        {
                            if (k != null)
                            {
                                string hv = (string)k.GetValue("JavaHome");
                                if (!string.IsNullOrEmpty(hv))
                                {
                                    string j = Path.Combine(hv, "bin", "java.exe");
                                    if (File.Exists(j)) return j;
                                }
                            }
                        }
                    }
                }
            }
            catch { }
            return "";
        }

        class LibResult
        {
            public List<string> Classpath = new List<string>();
            public List<string> Natives = new List<string>();
        }

        static LibResult ResolveLibraries(Dictionary<string, object> json)
        {
            var result = new LibResult();
            if (!json.ContainsKey("libraries")) return result;
            foreach (var item in (object[])json["libraries"])
            {
                var lib = (Dictionary<string, object>)item;
                if (!RuleAllowed(lib)) continue;
                string name = (string)lib["name"];

                Dictionary<string, object> downloads = null;
                if (lib.ContainsKey("downloads")) downloads = (Dictionary<string, object>)lib["downloads"];

                if (lib.ContainsKey("natives"))
                {
                    var natives = (Dictionary<string, object>)lib["natives"];
                    if (natives.ContainsKey("windows") && downloads != null && downloads.ContainsKey("classifiers"))
                    {
                        string cls = (string)natives["windows"];
                        var classifiers = (Dictionary<string, object>)downloads["classifiers"];
                        if (classifiers.ContainsKey(cls))
                        {
                            var cd = (Dictionary<string, object>)classifiers[cls];
                            string path = cd.ContainsKey("path")
                                ? (string)cd["path"] : MavenPath(name.Replace(":" + p2(name), "") + ":" + cls);
                            string url = cd.ContainsKey("url")
                                ? (string)cd["url"] : "https://libraries.minecraft.net/" + path;
                            string local = Path.Combine(InstallDir(), "libraries", path);
                            TryDownload(url, local);
                            result.Natives.Add(local);
                        }
                    }
                    continue;
                }

                Dictionary<string, object> artifact = downloads != null && downloads.ContainsKey("artifact")
                    ? (Dictionary<string, object>)downloads["artifact"] : null;
                string artPath = artifact != null && artifact.ContainsKey("path")
                    ? (string)artifact["path"] : MavenPath(name);
                string artUrl = artifact != null && artifact.ContainsKey("url")
                    ? (string)artifact["url"]
                    : (lib.ContainsKey("url") ? (string)lib["url"] : "https://libraries.minecraft.net/") + artPath;
                string artLocal = Path.Combine(InstallDir(), "libraries", artPath);
                TryDownload(artUrl, artLocal);
                // 1.21.9+ declares natives as separate artifact libraries (e.g. name ":natives-windows")
                // whose path contains "-natives-". They must be extracted to the natives dir, not the classpath.
                if (artPath.IndexOf("-natives-", StringComparison.Ordinal) >= 0)
                    result.Natives.Add(artLocal);
                else
                    result.Classpath.Add(artLocal);
            }
            return result;
        }

        static string p2(string name)
        {
            return name.Split(':')[2];
        }

        static void DownloadAssets(Dictionary<string, object> objects, string dir, Action<string> log)
        {
            int total = objects.Count, n = 0;
            foreach (var kv in objects)
            {
                var obj = (Dictionary<string, object>)kv.Value;
                string hash = (string)obj["hash"];
                string sub = hash.Substring(0, 2);
                string local = Path.Combine(dir, "assets", "objects", sub, hash);
                if (!File.Exists(local))
                {
                    TryDownload("https://resources.download.minecraft.net/" + sub + "/" + hash, local);
                }
                n++;
                if (n % 100 == 0) log("Ассеты: " + n + " / " + total);
                Progress = 50 + (n / (double)total) * 38.0;
            }
            log("Ассеты готовы (" + total + ").");
        }

        static void InstallFabricApi(string dir, Action<string> log)
        {
            var list = GetJsonArray("https://api.modrinth.com/v2/project/fabric-api/version?game_versions=%5B%221.21.11%22%5D");
            if (list.Length == 0) { log("Fabric API не найден"); return; }
            var top = (Dictionary<string, object>)list[0];
            var files = (object[])top["files"];
            if (files.Length == 0) { log("Fabric API: нет файла"); return; }
            var f = (Dictionary<string, object>)files[0];
            string url = (string)f["url"];
            string fn = (string)f["filename"];
            string local = Path.Combine(dir, "mods", fn);
            if (File.Exists(local) && new FileInfo(local).Length > 0)
            {
                log("Fabric API уже установлен.");
                return;
            }
            log("Скачиваю Fabric API: " + fn);
            DownloadUrl(url, local);
            log("Fabric API установлен.");
        }

        public static string SelectedModVersion()
        {
            string v = Config.SelectedVersion;
            return string.IsNullOrEmpty(v) ? MOD_VERSION : v;
        }

        public static string ModJarName(string version)
        {
            return "flash-visual-" + version + ".jar";
        }

        public static List<string> FetchVersions()
        {
            var list = new List<string>();
            try
            {
                var arr = GetJsonArray("https://api.github.com/repos/gitchkfffffffffffffffff/flash-visual/releases");
                foreach (object o in arr)
                {
                    var rel = (Dictionary<string, object>)o;
                    if (!rel.ContainsKey("assets")) continue;
                    foreach (object a in (object[])rel["assets"])
                    {
                        var fd = (Dictionary<string, object>)a;
                        string name = (string)fd["name"];
                        if (name == null || !name.StartsWith("flash-visual-") || !name.EndsWith(".jar")) continue;
                        string v = name.Substring("flash-visual-".Length);
                        v = v.Substring(0, v.Length - ".jar".Length);
                        if (!list.Contains(v)) list.Add(v);
                    }
                }
            }
            catch { }
            if (list.Count == 0) list.Add(MOD_VERSION);
            list.Sort(StringComparer.Ordinal);
            return list;
        }

        public static string ModUrl(string version)
        {
            return "https://github.com/gitchkfffffffffffffffff/flash-visual/releases/download/prerelis/"
                + ModJarName(version);
        }

        static void InstallModJar(string dir, Action<string> log)
        {
            string version = SelectedModVersion();
            string jarName = ModJarName(version);
            string local = Path.Combine(dir, "mods", jarName);
            if (File.Exists(local) && new FileInfo(local).Length > 0)
            {
                log("Мод уже установлен: " + jarName);
                return;
            }
            try
            {
                log("Скачиваю мод Flash Visual " + version + " с GitHub...");
                DownloadUrl(ModUrl(version), local);
                log("Мод установлен: " + jarName);
                return;
            }
            catch (Exception ex)
            {
                log("GitHub недоступен (" + ex.Message + "). Пробую локальный jar...");
            }
            string[] candidates =
            {
                Path.Combine(AppDomain.CurrentDomain.BaseDirectory, jarName),
                Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "build", "libs", jarName),
                Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "build", "libs", "flash-visual-1.0.0.jar")
            };
            string src = null;
            foreach (var c in candidates) if (File.Exists(c)) { src = c; break; }
            if (src == null)
            {
                log("Мод не найден — скопируйте " + jarName + " рядом с лаунчером.");
                return;
            }
            File.Copy(src, local, true);
            log("Мод установлен (локально): " + Path.GetFileName(src));
        }

        public static void DownloadGame(Action<string> log, Action<string> done)
        {
            var t = new Thread(() =>
            {
                try
                {
                    string dir = InstallDir();
                    foreach (var d in new[]
                    {
                        "libraries", "assets/indexes", "assets/objects",
                        "versions/" + MC_VERSION, "mods", "bin"
                    })
                    {
                        Directory.CreateDirectory(Path.Combine(dir, d));
                    }
                    log("Установка в: " + dir);

                    log("Загружаю версии...");
                    Progress = 5;
                    var man = GetJson("https://launchermeta.mojang.com/mc/game/version_manifest_v2.json");
                    string vUrl = null;
                    foreach (var v in (object[])man["versions"])
                    {
                        var vd = (Dictionary<string, object>)v;
                        if (MC_VERSION.Equals((string)vd["id"])) { vUrl = (string)vd["url"]; break; }
                    }
                    if (vUrl == null) { done("Не найдена версия " + MC_VERSION); return; }
                    var vanilla = GetJson(vUrl);
                    var fabric = GetJson(string.Format(
                        "https://meta.fabricmc.net/v2/versions/loader/{0}/{1}/profile/json", MC_VERSION, LOADER));

                    log("Загружаю библиотеки...");
                    Progress = 20;
                    var vLibs = ResolveLibraries(vanilla);
                    var fLibs = ResolveLibraries(fabric);
                    log("Библиотек: " + (vLibs.Classpath.Count + fLibs.Classpath.Count) +
                        " обычных, " + (vLibs.Natives.Count + fLibs.Natives.Count) + " нативных");

                    string jar = Path.Combine(dir, "versions", MC_VERSION, MC_VERSION + ".jar");
                    Progress = 40;
                    if (!File.Exists(jar) || new FileInfo(jar).Length == 0)
                    {
                        log("Загружаю client.jar...");
                        var dl = (Dictionary<string, object>)vanilla["downloads"];
                        DownloadUrl((string)((Dictionary<string, object>)dl["client"])["url"], jar);
                    }

                    string indexId = (string)((Dictionary<string, object>)vanilla["assetIndex"])["id"];
                    string indexLocal = Path.Combine(dir, "assets", "indexes", indexId + ".json");
                    Progress = 48;
                    if (!File.Exists(indexLocal))
                    {
                        log("Загружаю индексы ассетов...");
                        DownloadUrl((string)((Dictionary<string, object>)vanilla["assetIndex"])["url"], indexLocal);
                    }

                    log("Загружаю ассеты (звук, текстуры)...");
                    var idx = ParseJson(File.ReadAllText(indexLocal, Encoding.UTF8));
                    DownloadAssets((Dictionary<string, object>)idx["objects"], dir, log);

                    Progress = 90;
                    InstallFabricApi(dir, log);

                    Progress = 95;
                    InstallModJar(dir, log);

                    Progress = 100;
                    done("Установка завершена. Можно запускать.");
                }
                catch (Exception ex)
                {
                    done("Ошибка: " + ex.Message);
                }
            });
            t.IsBackground = true;
            t.Start();
        }

        public static void Launch(Action<string> log, Action<string> done)
        {
            var t = new Thread(() =>
            {
                try
                {
                    string dir = InstallDir();
                    var acc = Active();
                    if (acc == null) { done("Сначала добавьте аккаунт в разделе «Аккаунты»."); return; }
                    SyncAltsToMod();
                    string java = !string.IsNullOrEmpty(Config.JavaPath) && File.Exists(Config.JavaPath)
                        ? Config.JavaPath : AutoDetectJava();
                    if (string.IsNullOrEmpty(java) || !File.Exists(java))
                    {
                        done("Java не найдена. Укажите путь в «Настройках».");
                        return;
                    }

                    log("Поиск версии...");
                    Progress = 5;
                    var man = GetJson("https://launchermeta.mojang.com/mc/game/version_manifest_v2.json");
                    string vUrl = null;
                    foreach (var v in (object[])man["versions"])
                    {
                        var vd = (Dictionary<string, object>)v;
                        if (MC_VERSION.Equals((string)vd["id"])) { vUrl = (string)vd["url"]; break; }
                    }
                    var vanilla = GetJson(vUrl);
                    var fabric = GetJson(string.Format(
                        "https://meta.fabricmc.net/v2/versions/loader/{0}/{1}/profile/json", MC_VERSION, LOADER));

                    log("Проверяю библиотеки...");
                    Progress = 25;
                    var vLibs = ResolveLibraries(vanilla);
                    var fLibs = ResolveLibraries(fabric);

                    string jar = Path.Combine(dir, "versions", MC_VERSION, MC_VERSION + ".jar");
                    if (!File.Exists(jar) || new FileInfo(jar).Length == 0)
                    {
                        done("Игра не установлена. Нажмите «Скачать игру» на Главной.");
                        return;
                    }

                    string idxId = (string)((Dictionary<string, object>)vanilla["assetIndex"])["id"];
                    string idxLocal = Path.Combine(dir, "assets", "indexes", idxId + ".json");
                    Progress = 40;
                    if (File.Exists(idxLocal))
                    {
                        log("Проверяю ассеты...");
                        var idx = ParseJson(File.ReadAllText(idxLocal, Encoding.UTF8));
                        DownloadAssets((Dictionary<string, object>)idx["objects"], dir, log);
                    }

                    log("Готовлю нативные библиотеки...");
                    Progress = 60;
                    string nativesDir = Path.Combine(dir, "bin", "natives-" + MC_VERSION);
                    foreach (var nz in vLibs.Natives) ExtractZip(nz, nativesDir);
                    foreach (var nz in fLibs.Natives) ExtractZip(nz, nativesDir);

                    var cp = new List<string>();
                    cp.AddRange(vLibs.Classpath);
                    cp.AddRange(fLibs.Classpath);
                    cp.Add(jar);
                    string classpath = string.Join(";", cp);

                    string mainArgs =
                        "--assetsDir " + Q(Path.Combine(dir, "assets")) +
                        " --assetIndex " + idxId +
                        " --gameDir " + Q(dir) +
                        " --version " + MC_VERSION +
                        " --versionType release" +
                        " --width " + Config.Width +
                        " --height " + Config.Height +
                        " --username " + Q(acc.Name) +
                        " --uuid 00000000-0000-4000-8000-000000000000" +
                        " --accessToken 0 --userType legacy --userProperties {}";

                    string jvmArgs =
                        "-Xmx" + Config.RamMb + "M " +
                        "-Djava.library.path=" + Q(nativesDir) +
                        " -Dorg.lwjgl.util.StaticallyLinked=true" +
                        " -Dorg.lwjgl.system.SharedLibrarySeparator=false";

                    Progress = 85;
                    log("Запуск клиента...");
                    var psi = new ProcessStartInfo
                    {
                        FileName = java,
                        Arguments = jvmArgs + " -cp " + Q(classpath) +
                            " net.fabricmc.loader.impl.launch.knot.KnotClient " + mainArgs,
                        WorkingDirectory = dir,
                        UseShellExecute = false
                    };
                    var p = Process.Start(psi);
                    Progress = 100;
                    done("Игра запущена (pid " + p.Id + "). Мод Flash Visual активен.");
                }
                catch (Exception ex)
                {
                    done("Ошибка запуска: " + ex.Message);
                }
            });
            t.IsBackground = true;
            t.Start();
        }

        static string Q(string s)
        {
            return "\"" + s + "\"";
        }
    }
}
