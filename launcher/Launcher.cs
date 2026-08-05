using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.IO;
using System.Runtime.InteropServices;
using System.Text;
using System.Windows.Forms;

namespace FlashVisualLauncher
{
    public class LauncherForm : Form
    {
        private static readonly Color BG = Color.FromArgb(7, 10, 18);
        private static readonly Color SIDEBAR = Color.FromArgb(11, 16, 28);
        private static readonly Color PANEL = Color.FromArgb(15, 22, 38);
        private static readonly Color ACCENT = Color.FromArgb(0, 207, 255);
        private static readonly Color TEXT = Color.FromArgb(222, 228, 240);
        private static readonly Color SUB = Color.FromArgb(122, 134, 154);

        private TextBox logBox;
        private PulseButton btnPlay;
        private PulseButton btnDownload;
        private PulseButton btnNavMain;
        private PulseButton btnNavAccounts;
        private PulseButton btnNavSettings;
        private PulseButton btnNavMods;

        private TextBox txtAccount;
        private ListBox lstAccounts;
        private PulseButton btnAddAccount;
        private PulseButton btnUseAccount;
        private PulseButton btnDelAccount;

        private TrackBar ramTrack;
        private Label ramLabel;
        private TextBox txtJava;
        private TextBox txtResW;
        private TextBox txtResH;
        private TextBox txtDir;
        private PulseButton btnJavaAuto;
        private PulseButton btnJavaBrowse;
        private PulseButton btnDirBrowse;
        private PulseButton btnDirDefault;
        private Label lblAccount;
        private Label lblDir;

        private ListBox lstMods;
        private Label lblModStatus;
        private PulseButton btnModRefresh;
        private PulseButton btnModOpen;
        private PulseButton btnModToggle;
        private PulseButton btnModDelete;
        private readonly List<string> modFiles = new List<string>();

        private int nav = 0;
        private bool busy;

        [DllImport("user32.dll")]
        private static extern bool ReleaseCapture();

        [DllImport("user32.dll")]
        private static extern int SendMessage(IntPtr hWnd, int msg, int wParam, int lParam);

        private const int WM_NCLBUTTONDOWN = 0xA1;
        private const int HTCAPTION = 2;

        public LauncherForm()
        {
            Core.LoadConfig();
            if (Core.Config.Accounts.Count == 0)
            {
                Core.Config.Accounts.Add(new LAccount { Name = "Steve", Type = "offline" });
                Core.Config.ActiveAccount = 0;
                Core.SaveConfig();
            }

            Text = "Flash Visual — лаунчер";
            FormBorderStyle = FormBorderStyle.None;
            ClientSize = new Size(960, 620);
            StartPosition = FormStartPosition.CenterScreen;
            BackColor = BG;
            Font = new Font("Segoe UI", 10f);
            DoubleBuffered = true;
            SetStyle(ControlStyles.AllPaintingInWmPaint | ControlStyles.UserPaint
                | ControlStyles.OptimizedDoubleBuffer | ControlStyles.ResizeRedraw, true);

            BuildNav();
            BuildHome();
            BuildMods();
            BuildAccounts();
            BuildSettings();
            BuildConsole();

            switchPage(0);
            Core.SyncAltsToMod();
            Append("Flash Visual launcher · " + Core.MOD_VERSION + " · Fabric 1.21.11");
            Append("Директория установки: " + Core.InstallDir());
        }

        private void BuildNav()
        {
            btnNavMain = MakeNav("ГЛАВНАЯ", 12, 74, 0);
            btnNavMods = MakeNav("МОДЫ", 12, 108, 1);
            btnNavAccounts = MakeNav("АККАУНТЫ", 12, 142, 2);
            btnNavSettings = MakeNav("НАСТРОЙКИ", 12, 176, 3);
        }

        private PulseButton MakeNav(string text, int x, int y, int page)
        {
            var b = new PulseButton
            {
                Text = text,
                Nav = true,
                NavActive = page == 0,
                Location = new Point(x, y),
                Size = new Size(172, 32),
                Font = new Font("Segoe UI", 10f, FontStyle.Bold),
                FlatStyle = FlatStyle.Flat,
                FlatAppearance = { BorderSize = 0 },
                ForeColor = SUB
            };
            b.Click += (s, e) => switchPage(page);
            Controls.Add(b);
            return b;
        }

        private void BuildHome()
        {
            lblAccount = new Label
            {
                Location = new Point(198, 268),
                Size = new Size(460, 26),
                ForeColor = TEXT,
                Text = ""
            };
            lblDir = new Label
            {
                Location = new Point(198, 294),
                Size = new Size(700, 24),
                ForeColor = SUB,
                Text = ""
            };
            Controls.Add(lblAccount);
            Controls.Add(lblDir);

            btnPlay = new PulseButton
            {
                Text = "ИГРАТЬ",
                Primary = true,
                Location = new Point(198, 140),
                Size = new Size(300, 56),
                Font = new Font("Segoe UI", 14f, FontStyle.Bold),
                FlatStyle = FlatStyle.Flat,
                FlatAppearance = { BorderSize = 0 }
            };
            btnPlay.Click += (s, e) => Run("запуск", Core.Launch, "Игра запускается...");

            btnDownload = new PulseButton
            {
                Text = "СКАЧАТЬ ИГРУ",
                Outline = true,
                ForeColor = TEXT,
                Location = new Point(198, 210),
                Size = new Size(300, 40),
                FlatStyle = FlatStyle.Flat,
                FlatAppearance = { BorderSize = 0 }
            };
            btnDownload.Click += (s, e) => Run("установка", Core.DownloadGame, "Установка начата...");

            Controls.Add(btnPlay);
            Controls.Add(btnDownload);
        }

        private void BuildMods()
        {
            lblModStatus = new Label
            {
                Location = new Point(198, 110),
                Size = new Size(460, 22),
                ForeColor = SUB,
                Text = "Моды не загружены"
            };
            lstMods = new ListBox
            {
                Location = new Point(198, 140),
                Size = new Size(420, 180),
                BackColor = Color.FromArgb(12, 20, 36),
                ForeColor = TEXT,
                BorderStyle = BorderStyle.FixedSingle,
                Font = new Font("Segoe UI", 10.5f)
            };
            btnModRefresh = MakeOutlineBtn("Обновить", 198, 332, 100);
            btnModOpen = MakeOutlineBtn("Открыть папку", 306, 332, 120);
            btnModToggle = MakeOutlineBtn("Вкл / Выкл", 434, 332, 100);
            btnModDelete = MakeOutlineBtn("Удалить", 542, 332, 100);

            btnModRefresh.Click += (s, e) => RefreshMods();
            btnModOpen.Click += (s, e) =>
            {
                string dir = Path.Combine(Core.InstallDir(), "mods");
                Directory.CreateDirectory(dir);
                Process.Start("explorer.exe", dir);
            };
            btnModToggle.Click += (s, e) => ToggleMod();
            btnModDelete.Click += (s, e) => DeleteMod();

            Controls.Add(lblModStatus);
            Controls.Add(lstMods);
            Controls.Add(btnModRefresh);
            Controls.Add(btnModOpen);
            Controls.Add(btnModToggle);
            Controls.Add(btnModDelete);
        }

        private PulseButton MakeOutlineBtn(string text, int x, int y, int w)
        {
            var b = new PulseButton
            {
                Text = text,
                Outline = true,
                ForeColor = Color.FromArgb(222, 228, 240),
                Location = new Point(x, y),
                Size = new Size(w, 34),
                FlatStyle = FlatStyle.Flat,
                FlatAppearance = { BorderSize = 0 }
            };
            return b;
        }

        private void RefreshMods()
        {
            modFiles.Clear();
            lstMods.Items.Clear();
            string dir = Path.Combine(Core.InstallDir(), "mods");
            if (!Directory.Exists(dir))
            {
                lblModStatus.Text = "Папка mods не найдена — нажмите «Скачать игру» на Главной.";
                return;
            }
            foreach (string f in Directory.GetFiles(dir, "*.jar"))
            {
                modFiles.Add(Path.GetFileName(f));
            }
            foreach (string f in Directory.GetFiles(dir, "*.jar.disabled"))
            {
                modFiles.Add(Path.GetFileName(f).Replace(".jar.disabled", ".jar"));
            }
            modFiles.Sort(StringComparer.OrdinalIgnoreCase);
            foreach (string m in modFiles)
            {
                string full = Path.Combine(dir, m);
                bool on = File.Exists(full) && !File.Exists(full + ".disabled");
                lstMods.Items.Add((on ? "✓ " : "✕ ") + m);
            }
            lblModStatus.Text = "Модов: " + modFiles.Count;
        }

        private void ToggleMod()
        {
            int i = lstMods.SelectedIndex;
            if (i < 0 || i >= modFiles.Count) { Append("Выберите мод из списка."); return; }
            string dir = Path.Combine(Core.InstallDir(), "mods");
            string name = modFiles[i];
            string full = Path.Combine(dir, name);
            string dis = full + ".disabled";
            if (File.Exists(full) && !File.Exists(dis))
            {
                File.Move(full, dis);
                Append("Выключен: " + name);
            }
            else if (File.Exists(dis))
            {
                File.Move(dis, full);
                Append("Включён: " + name);
            }
            RefreshMods();
        }

        private void DeleteMod()
        {
            int i = lstMods.SelectedIndex;
            if (i < 0 || i >= modFiles.Count) { Append("Выберите мод из списка."); return; }
            string dir = Path.Combine(Core.InstallDir(), "mods");
            string name = modFiles[i];
            string full = Path.Combine(dir, name);
            if (File.Exists(full)) File.Delete(full);
            string dis = full + ".disabled";
            if (File.Exists(dis)) File.Delete(dis);
            Append("Удалён: " + name);
            RefreshMods();
        }

        private void BuildAccounts()
        {
            txtAccount = new TextBox
            {
                Location = new Point(198, 110),
                Size = new Size(300, 28),
                BackColor = Color.FromArgb(12, 20, 36),
                ForeColor = TEXT,
                BorderStyle = BorderStyle.FixedSingle,
                Font = new Font("Segoe UI", 11f)
            };
            txtAccount.SetBounds(txtAccount.Left, txtAccount.Top, 300, 30);

            btnAddAccount = new PulseButton
            {
                Text = "Добавить",
                Outline = true,
                ForeColor = TEXT,
                Location = new Point(506, 108),
                Size = new Size(120, 34),
                FlatStyle = FlatStyle.Flat,
                FlatAppearance = { BorderSize = 0 }
            };
            btnAddAccount.Click += (s, e) => AddAccount();

            lstAccounts = new ListBox
            {
                Location = new Point(198, 156),
                Size = new Size(300, 150),
                BackColor = Color.FromArgb(12, 20, 36),
                ForeColor = TEXT,
                BorderStyle = BorderStyle.FixedSingle,
                Font = new Font("Segoe UI", 10.5f)
            };

            btnUseAccount = new PulseButton
            {
                Text = "Выбрать",
                Outline = true,
                ForeColor = TEXT,
                Location = new Point(198, 318),
                Size = new Size(100, 34),
                FlatStyle = FlatStyle.Flat,
                FlatAppearance = { BorderSize = 0 }
            };
            btnUseAccount.Click += (s, e) => UseAccount();

            btnDelAccount = new PulseButton
            {
                Text = "Удалить",
                Outline = true,
                ForeColor = TEXT,
                Location = new Point(306, 318),
                Size = new Size(100, 34),
                FlatStyle = FlatStyle.Flat,
                FlatAppearance = { BorderSize = 0 }
            };
            btnDelAccount.Click += (s, e) => DeleteAccount();

            Controls.Add(txtAccount);
            Controls.Add(btnAddAccount);
            Controls.Add(lstAccounts);
            Controls.Add(btnUseAccount);
            Controls.Add(btnDelAccount);
        }

        private void BuildSettings()
        {
            MakeSettingLabel("ПАМЯТЬ (RAM)", 198, 84);
            ramLabel = new Label
            {
                Location = new Point(460, 86),
                Size = new Size(120, 22),
                ForeColor = ACCENT,
                Text = Core.Config.RamMb + " МБ"
            };
            ramTrack = new TrackBar
            {
                Location = new Point(198, 108),
                Size = new Size(360, 40),
                Minimum = 512,
                Maximum = 8192,
                TickFrequency = 512,
                SmallChange = 256,
                LargeChange = 1024,
                Value = Core.Config.RamMb
            };
            ramTrack.ValueChanged += (s, e) =>
            {
                Core.Config.RamMb = ramTrack.Value;
                ramLabel.Text = ramTrack.Value + " МБ";
                Core.SaveConfig();
            };
            Controls.Add(ramLabel);
            Controls.Add(ramTrack);

            MakeSettingLabel("ПУТЬ К JAVA", 198, 158);
            txtJava = new TextBox
            {
                Location = new Point(198, 184),
                Size = new Size(460, 28),
                BackColor = Color.FromArgb(12, 20, 36),
                ForeColor = TEXT,
                BorderStyle = BorderStyle.FixedSingle,
                Text = Core.Config.JavaPath
            };
            txtJava.TextChanged += (s, e) => { Core.Config.JavaPath = txtJava.Text; Core.SaveConfig(); };
            btnJavaAuto = new PulseButton
            {
                Text = "Авто",
                Outline = true,
                ForeColor = TEXT,
                Location = new Point(666, 182),
                Size = new Size(70, 32),
                FlatStyle = FlatStyle.Flat,
                FlatAppearance = { BorderSize = 0 }
            };
            btnJavaAuto.Click += (s, e) => txtJava.Text = Core.AutoDetectJava();
            btnJavaBrowse = new PulseButton
            {
                Text = "Обзор",
                Outline = true,
                ForeColor = TEXT,
                Location = new Point(744, 182),
                Size = new Size(90, 32),
                FlatStyle = FlatStyle.Flat,
                FlatAppearance = { BorderSize = 0 }
            };
            btnJavaBrowse.Click += (s, e) =>
            {
                using (var dlg = new OpenFileDialog())
                {
                    dlg.Filter = "java.exe|java.exe";
                    if (dlg.ShowDialog() == DialogResult.OK) txtJava.Text = dlg.FileName;
                }
            };
            Controls.Add(txtJava);
            Controls.Add(btnJavaAuto);
            Controls.Add(btnJavaBrowse);

            MakeSettingLabel("РАЗРЕШЕНИЕ", 198, 232);
            txtResW = new TextBox
            {
                Location = new Point(198, 258),
                Size = new Size(80, 28),
                BackColor = Color.FromArgb(12, 20, 36),
                ForeColor = TEXT,
                BorderStyle = BorderStyle.FixedSingle,
                Text = Core.Config.Width.ToString()
            };
            txtResH = new TextBox
            {
                Location = new Point(290, 258),
                Size = new Size(80, 28),
                BackColor = Color.FromArgb(12, 20, 36),
                ForeColor = TEXT,
                BorderStyle = BorderStyle.FixedSingle,
                Text = Core.Config.Height.ToString()
            };
            txtResW.TextChanged += (s, e) => { int v; if (int.TryParse(txtResW.Text, out v)) { Core.Config.Width = v; Core.SaveConfig(); } };
            txtResH.TextChanged += (s, e) => { int v; if (int.TryParse(txtResH.Text, out v)) { Core.Config.Height = v; Core.SaveConfig(); } };
            Controls.Add(txtResW);
            Controls.Add(txtResH);

            MakeSettingLabel("ДИРЕКТОРИЯ УСТАНОВКИ (как .minecraft)", 198, 306);
            txtDir = new TextBox
            {
                Location = new Point(198, 332),
                Size = new Size(460, 28),
                BackColor = Color.FromArgb(12, 20, 36),
                ForeColor = TEXT,
                BorderStyle = BorderStyle.FixedSingle,
                Text = Core.Config.InstallDir
            };
            txtDir.TextChanged += (s, e) => { Core.Config.InstallDir = txtDir.Text; Core.SaveConfig(); };
            btnDirBrowse = new PulseButton
            {
                Text = "Обзор",
                Outline = true,
                ForeColor = TEXT,
                Location = new Point(666, 330),
                Size = new Size(90, 32),
                FlatStyle = FlatStyle.Flat,
                FlatAppearance = { BorderSize = 0 }
            };
            btnDirBrowse.Click += (s, e) =>
            {
                using (var dlg = new FolderBrowserDialog())
                {
                    dlg.Description = "Выберите папку установки (.minecraft)";
                    if (dlg.ShowDialog() == DialogResult.OK) txtDir.Text = dlg.SelectedPath;
                }
            };
            btnDirDefault = new PulseButton
            {
                Text = "По умолчанию",
                Outline = true,
                ForeColor = TEXT,
                Location = new Point(764, 330),
                Size = new Size(120, 32),
                FlatStyle = FlatStyle.Flat,
                FlatAppearance = { BorderSize = 0 }
            };
            btnDirDefault.Click += (s, e) => txtDir.Text = "";
            Controls.Add(txtDir);
            Controls.Add(btnDirBrowse);
            Controls.Add(btnDirDefault);
        }

        private void MakeSettingLabel(string text, int x, int y)
        {
            var l = new Label
            {
                Text = text,
                Location = new Point(x, y),
                AutoSize = true,
                ForeColor = SUB,
                Font = new Font("Segoe UI", 9f, FontStyle.Bold)
            };
            Controls.Add(l);
        }

        private void BuildConsole()
        {
            logBox = new TextBox
            {
                Multiline = true,
                ReadOnly = true,
                BorderStyle = BorderStyle.None,
                ScrollBars = ScrollBars.Vertical,
                BackColor = PANEL,
                ForeColor = Color.FromArgb(200, 210, 226),
                Font = new Font("Consolas", 9.5f),
                WordWrap = false,
                Location = new Point(198 + 6, 452),
                Size = new Size(ClientSize.Width - 198 - 24, ClientSize.Height - 452 - 20)
            };
            Controls.Add(logBox);
        }

        private void switchPage(int p)
        {
            nav = p;
            foreach (var b in new[] { btnNavMain, btnNavMods, btnNavAccounts, btnNavSettings })
            {
                bool active = b == btnNavMain && p == 0 || b == btnNavMods && p == 1
                    || b == btnNavAccounts && p == 2 || b == btnNavSettings && p == 3;
                b.NavActive = active;
                b.Invalidate();
            }
            RefreshAccountList();
            bool home = p == 0, mods = p == 1, acc = p == 2, set = p == 3;
            lblAccount.Visible = home;
            lblDir.Visible = home;
            btnPlay.Visible = home;
            btnDownload.Visible = home;
            lblModStatus.Visible = mods;
            lstMods.Visible = mods;
            btnModRefresh.Visible = mods;
            btnModOpen.Visible = mods;
            btnModToggle.Visible = mods;
            btnModDelete.Visible = mods;
            if (mods) RefreshMods();
            txtAccount.Visible = acc;
            btnAddAccount.Visible = acc;
            lstAccounts.Visible = acc;
            btnUseAccount.Visible = acc;
            btnDelAccount.Visible = acc;
            ramLabel.Visible = set;
            ramTrack.Visible = set;
            txtJava.Visible = set;
            btnJavaAuto.Visible = set;
            btnJavaBrowse.Visible = set;
            txtResW.Visible = set;
            txtResH.Visible = set;
            txtDir.Visible = set;
            btnDirBrowse.Visible = set;
            btnDirDefault.Visible = set;
            Invalidate();
        }

        private void RefreshAccountList()
        {
            var acc = Core.Active();
            lblAccount.Text = acc == null ? "Аккаунт: —" : "Аккаунт: " + acc.Name + "   [" + acc.Type + "]";
            lblDir.Text = "Установка: " + Core.InstallDir();
            int sel = lstAccounts.SelectedIndex;
            lstAccounts.Items.Clear();
            int i = 0;
            foreach (var a in Core.Config.Accounts)
            {
                lstAccounts.Items.Add((i == Core.Config.ActiveAccount ? "✓ " : "    ") + a.Name);
                i++;
            }
            if (sel >= 0 && sel < lstAccounts.Items.Count) lstAccounts.SelectedIndex = sel;
            if (lstAccounts.Items.Count > 0 && lstAccounts.SelectedIndex < 0) lstAccounts.SelectedIndex = Core.Config.ActiveAccount;
        }

        private void AddAccount()
        {
            string name = txtAccount.Text.Trim();
            if (name.Length == 0) { Append("Введите ник."); return; }
            Core.Config.Accounts.Add(new LAccount { Name = name, Type = "offline" });
            Core.SaveConfig();
            Core.SyncAltsToMod();
            RefreshAccountList();
            txtAccount.Text = "";
            Append("Аккаунт добавлен: " + name);
        }

        private void UseAccount()
        {
            int i = lstAccounts.SelectedIndex;
            if (i < 0 || i >= Core.Config.Accounts.Count) { Append("Выберите аккаунт из списка."); return; }
            Core.Config.ActiveAccount = i;
            Core.SaveConfig();
            Core.SyncAltsToMod();
            RefreshAccountList();
            Append("Выбран аккаунт: " + Core.Config.Accounts[i].Name);
        }

        private void DeleteAccount()
        {
            int i = lstAccounts.SelectedIndex;
            if (i < 0 || i >= Core.Config.Accounts.Count) { Append("Выберите аккаунт из списка."); return; }
            string name = Core.Config.Accounts[i].Name;
            Core.Config.Accounts.RemoveAt(i);
            if (Core.Config.ActiveAccount >= Core.Config.Accounts.Count) Core.Config.ActiveAccount = 0;
            Core.SaveConfig();
            Core.SyncAltsToMod();
            RefreshAccountList();
            Append("Аккаунт удалён: " + name);
        }

        private void Run(string what, Action<Action<string>, Action<string>> job, string startMsg)
        {
            if (busy) { Append("Операция уже выполняется."); return; }
            busy = true;
            btnPlay.Enabled = false;
            btnDownload.Enabled = false;
            Invalidate();
            Append(startMsg);
            job(Append, msg =>
            {
                Append(msg);
                busy = false;
                btnPlay.Enabled = true;
                btnDownload.Enabled = true;
                Invalidate();
            });
        }

        protected override void OnPaint(PaintEventArgs e)
        {
            base.OnPaint(e);
            var g = e.Graphics;
            g.SmoothingMode = SmoothingMode.AntiAlias;

            using (var bg = new LinearGradientBrush(ClientRectangle, BG, Color.FromArgb(10, 14, 26), 90f))
            {
                g.FillRectangle(bg, ClientRectangle);
            }

            g.FillRectangle(new SolidBrush(SIDEBAR), 0, 40, 190, Height - 40);
            g.FillRectangle(new SolidBrush(Color.FromArgb(18, 26, 44)), 190, 40, 1, Height - 40);
            g.FillRectangle(new SolidBrush(ACCENT), 0, 0, 190, 2);
            g.FillRectangle(new SolidBrush(Color.FromArgb(16, 24, 44)), 190, 0, Width - 190, 1);

            Rectangle logo = new Rectangle(14, 8, 26, 26);
            using (var path = RoundRect(logo, 7))
            using (var brush = new SolidBrush(ACCENT))
            {
                g.FillPath(brush, path);
            }
            g.DrawString("F", new Font("Segoe UI", 13f, FontStyle.Bold), Brushes.Black, 21, 10);
            g.DrawString("FLASH", new Font("Segoe UI", 11f, FontStyle.Bold), new SolidBrush(TEXT), 46, 10);
            g.DrawString("VISUAL", new Font("Segoe UI", 11f, FontStyle.Bold), new SolidBrush(ACCENT), 92, 10);
            g.DrawString("—", new Font("Segoe UI", 14f), new SolidBrush(SUB), Width - 62, 8);
            g.DrawString("✕", new Font("Segoe UI", 13f), new SolidBrush(SUB), Width - 31, 9);

            g.DrawString(Core.MOD_VERSION + " · MC " + Core.MC_VERSION, new Font("Segoe UI", 9f), new SolidBrush(SUB), 12, Height - 34);
            g.DrawString("Fabric " + Core.LOADER, new Font("Segoe UI", 9f), new SolidBrush(SUB), 12, Height - 20);

            if (nav == 0)
            {
                string status = busy ? "Выполняется…" : "Готов к запуску";
                g.DrawString(status, new Font("Segoe UI", 20f, FontStyle.Bold), new SolidBrush(TEXT), 198, 70);
                using (var line = new LinearGradientBrush(new Rectangle(198, 132, 300, 2), ACCENT, Color.FromArgb(0, 80, 120), 0f))
                {
                    g.FillRectangle(line, 198, 132, 300, 2);
                }
            }
            else if (nav == 1)
            {
                g.DrawString("МОДЫ", new Font("Segoe UI", 20f, FontStyle.Bold), new SolidBrush(TEXT), 198, 70);
                g.DrawString("Мод Flash Visual устанавливается автоматически", new Font("Segoe UI", 10f), new SolidBrush(SUB), 198, 110);
                g.DrawString("в папку mods директории установки.", new Font("Segoe UI", 10f), new SolidBrush(SUB), 198, 132);
                g.DrawString("Также ставится Fabric API.", new Font("Segoe UI", 10f), new SolidBrush(SUB), 198, 154);
            }
            else if (nav == 2)
            {
                g.DrawString("АККАУНТЫ", new Font("Segoe UI", 20f, FontStyle.Bold), new SolidBrush(TEXT), 198, 70);
                g.DrawString("Оффлайн-аккаунты (ник). Войдите как в обычном лаунчере.", new Font("Segoe UI", 9.5f), new SolidBrush(SUB), 198, 96);
            }
            else if (nav == 3)
            {
                g.DrawString("НАСТРОЙКИ", new Font("Segoe UI", 20f, FontStyle.Bold), new SolidBrush(TEXT), 198, 70);
            }

            Rectangle console = new Rectangle(198, 430, ClientSize.Width - 222, ClientSize.Height - 452);
            g.FillRectangle(new SolidBrush(PANEL), console);
            g.DrawRectangle(new Pen(Color.FromArgb(30, 44, 70), 1f), console);
            g.DrawString("КОНСОЛЬ", new Font("Segoe UI", 9f, FontStyle.Bold), new SolidBrush(SUB), console.X + 8, console.Y - 18);
        }

        protected override void OnMouseDown(MouseEventArgs e)
        {
            base.OnMouseDown(e);
            if (e.Button == MouseButtons.Left && e.Y < 40)
            {
                ReleaseCapture();
                SendMessage(Handle, WM_NCLBUTTONDOWN, HTCAPTION, 0);
            }
        }

        protected override void OnMouseUp(MouseEventArgs e)
        {
            base.OnMouseUp(e);
            if (e.Button != MouseButtons.Left || e.Y >= 40) return;
            if (e.X > Width - 44 && e.X < Width - 18) Close();
            else if (e.X > Width - 74 && e.X < Width - 48) WindowState = FormWindowState.Minimized;
        }

        private void Append(string line)
        {
            if (line == null) return;
            if (InvokeRequired)
            {
                BeginInvoke((Action)(() => Append(line)));
                return;
            }
            logBox.AppendText(line + Environment.NewLine);
            logBox.SelectionStart = logBox.TextLength;
            logBox.ScrollToCaret();
        }

        internal static GraphicsPath RoundRect(Rectangle r, int radius)
        {
            int d = radius * 2;
            var path = new GraphicsPath();
            path.AddArc(r.X, r.Y, d, d, 180, 90);
            path.AddArc(r.Right - d, r.Y, d, d, 270, 90);
            path.AddArc(r.Right - d, r.Bottom - d, d, d, 0, 90);
            path.AddArc(r.X, r.Bottom - d, d, d, 90, 90);
            path.CloseFigure();
            return path;
        }
    }

    public class PulseButton : Button
    {
        private bool hover;
        public bool Primary { get; set; }
        public bool Outline { get; set; }
        public bool Nav { get; set; }
        public bool NavActive { get; set; }

        private static readonly Color Accent = Color.FromArgb(0, 207, 255);
        private static readonly Color Side = Color.FromArgb(11, 16, 28);

        public PulseButton()
        {
            SetStyle(ControlStyles.AllPaintingInWmPaint | ControlStyles.UserPaint
                | ControlStyles.OptimizedDoubleBuffer | ControlStyles.ResizeRedraw, true);
        }

        protected override void OnMouseEnter(EventArgs e) { hover = true; Invalidate(); base.OnMouseEnter(e); }
        protected override void OnMouseLeave(EventArgs e) { hover = false; Invalidate(); base.OnMouseLeave(e); }

        protected override void OnPaint(PaintEventArgs e)
        {
            var g = e.Graphics;
            g.SmoothingMode = SmoothingMode.AntiAlias;
            Rectangle r = new Rectangle(0, 0, Width - 1, Height - 1);
            Color textColor = Enabled ? ForeColor : Color.FromArgb(122, 134, 154);

            if (Nav)
            {
                Color c = NavActive ? Color.FromArgb(20, 34, 58) : (hover ? Color.FromArgb(15, 24, 42) : Side);
                g.FillRectangle(new SolidBrush(c), r);
                if (NavActive) g.FillRectangle(new SolidBrush(Accent), 0, 0, 3, Height);
                g.DrawString(Text, Font, new SolidBrush(NavActive ? Accent : textColor), 18, (Height - Font.Height) / 2 - 1);
                return;
            }

            using (var path = LauncherForm.RoundRect(r, 10))
            {
                if (Primary)
                {
                    Color baseC = hover && Enabled ? Color.FromArgb(60, 222, 255) : Accent;
                    using (var bg = new LinearGradientBrush(r, baseC, Color.FromArgb(0, 150, 210), 90f))
                    {
                        g.FillPath(bg, path);
                    }
                    g.DrawString(Text, Font, Brushes.Black, (Width - g.MeasureString(Text, Font).Width) / 2f, (Height - Font.Height) / 2f - 1);
                }
                else
                {
                    Color line = hover && Enabled ? Accent : Color.FromArgb(48, 66, 96);
                    using (var fill = new SolidBrush(hover ? Color.FromArgb(18, 28, 48) : Color.FromArgb(12, 20, 36)))
                    {
                        g.FillPath(fill, path);
                    }
                    using (var pen = new Pen(line, 1.4f))
                    {
                        g.DrawPath(pen, path);
                    }
                    g.DrawString(Text, Font, new SolidBrush(textColor), (Width - g.MeasureString(Text, Font).Width) / 2f, (Height - Font.Height) / 2f - 1);
                }
            }
        }
    }

    internal static class Program
    {
        [STAThread]
        private static void Main()
        {
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            Application.Run(new LauncherForm());
        }
    }
}
