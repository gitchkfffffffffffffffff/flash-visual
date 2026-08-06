Add-Type -AssemblyName System.Drawing

$bmp = New-Object System.Drawing.Bitmap 64,64
# прозрачный фон (alpha 0)
for ($y=0; $y -lt 64; $y++){ for($x=0;$x -lt 64;$x++){ $bmp.SetPixel($x,$y,[System.Drawing.Color]::FromArgb(0,0,0,0)) } }

function P($x,$y,$a){ if($x -ge 0 -and $x -lt 64 -and $y -ge 0 -and $y -lt 64){ $bmp.SetPixel($x,$y,$a) } }
function Fill($x,$y,$w,$h,$c){ for($iy=$y;$iy -lt ($y+$h);$iy++){ for($ix=$x;$ix -lt ($x+$w);$ix++){ P $ix $iy $c } } }

$skin=[System.Drawing.Color]::FromArgb(244,201,165)
$skinDk=[System.Drawing.Color]::FromArgb(232,187,150)
$hair=[System.Drawing.Color]::FromArgb(250,205,225)
$hairDk=[System.Drawing.Color]::FromArgb(235,170,195)
$outfit=[System.Drawing.Color]::FromArgb(250,240,248)
$skirt=[System.Drawing.Color]::FromArgb(255,186,198)
$sock=[System.Drawing.Color]::FromArgb(255,246,250)
$shoe=[System.Drawing.Color]::FromArgb(216,156,180)
$eyes=[System.Drawing.Color]::FromArgb(84,66,150)
$white=[System.Drawing.Color]::FromArgb(255,255,255)
$blush=[System.Drawing.Color]::FromArgb(255,174,188)
$mouthR=[System.Drawing.Color]::FromArgb(214,118,124)

# ===== HEAD (classic 64x64, front face 8,8-15,15) =====
# top of head (8,0)-(15,7): волосы
Fill 8 0 8 8 $hair
# bottom of head (16,0)-(23,7): затылок — волосы
Fill 16 0 8 8 $hairDk
# right side of head (0,8)-(7,15): волосы справа
Fill 0 8 8 8 $hairDk
# left side (16,8)-(23,15): прядь волос слева + кожа
Fill 16 8 8 8 $skin
Fill 16 8 2 8 $hair   # прядь волос по краю слева

# front face (8,8)-(15,15):
Fill 8 8 8 8 $skin
# чёлка-волосы сверху
Fill 7 8 10 3 $hair     # захватывая край
# ушки фурри (два треугольника), поверх макушки front area top rows
P 8 8 $hairDk; P 9 8 $hairDk
P 12 8 $hair; P 13 8 $hair
P 8 7 $hair; P 9 7 $hair  # выступы ушек выше лица (визуально)
# боковые пряди челки
P 8 9 $hair; P 8 10 $hair; P 15 8 $hair; P 15 9 $hair; P 15 10 $hair
# большие глаза (2x2) с белыми бликами
for($gy=11;$gy -lt 13;$gy++){ Fill 9 $gy 2 1 $eyes; Fill 12 $gy 2 1 $eyes }
P 9 11 $white; P 12 11 $white
# румянец
Fill 9 13 2 1 $blush; Fill 12 13 2 1 $blush
# рот
P 11 14 $mouthR; P 12 14 $mouthR

# back head (24,8)-(31,15): длинные волосы
Fill 24 8 8 8 $hair
Fill 25 9 1 6 $hairDk; Fill 30 9 1 6 $hairDk

# ===== BODY (16,20 - 39,31 classic) =====
# top body
Fill 20 16 8 4 $outfit
Fill 28 16 8 4 $skirt
# right side
Fill 16 20 4 12 $skirt
# left side
Fill 28 20 4 12 $skirt
# back (long hair) 32,20
Fill 32 20 8 12 $hair
Fill 33 26 1 4 $hairDk; Fill 38 26 1 4 $hairDk
# front 20,20-27,31: блуза + юбка
Fill 20 20 8 5 $outfit
Fill 20 25 8 6 $skirt
# бант на блузе
P 22 21 $hairDk; P 23 21 $hairDk; P 22 22 $hairDk; P 23 22 $hairDk; P 22 23 $hair; P 23 23 $hair
# byuzovy воротник
Fill 23 20 2 1 $white

# ===== RIGHT ARM (40,20 - 55,31) =====
Fill 44 16 4 4 $outfit
Fill 48 16 4 4 $outfit
Fill 40 20 4 12 $outfit      # right
Fill 44 20 4 5 $outfit       # front рукав
Fill 44 25 4 6 $skin         # кисть
Fill 48 20 4 12 $outfit      # left
Fill 52 20 4 12 $outfit      # back

# ===== RIGHT LEG (0,20 - 15,31) =====
Fill 4 16 4 4 $skirt
Fill 8 16 4 4 $shoe
Fill 0 20 4 12 $skirt
Fill 4 20 4 4 $skirt
Fill 4 24 4 5 $sock
Fill 4 29 4 2 $shoe
Fill 8 20 4 12 $skirt
Fill 12 20 4 4 $sock
Fill 12 24 4 5 $sock
Fill 12 29 4 2 $shoe

# ===== LEFT ARM overlay (36,48 - 47,63) =====
Fill 36 48 4 4 $outfit
Fill 40 48 4 4 $outfit
Fill 32 52 4 12 $outfit
Fill 36 52 4 5 $outfit
Fill 36 57 4 6 $skin
Fill 40 52 4 12 $outfit
Fill 44 52 4 12 $outfit

# ===== LEFT LEG overlay (16,52 - 31,63) =====
Fill 20 48 4 4 $skirt
Fill 24 48 4 4 $shoe
Fill 16 52 4 12 $skirt
Fill 20 52 4 4 $skirt
Fill 20 56 4 5 $sock
Fill 20 61 4 2 $shoe
Fill 24 52 4 12 $skirt
Fill 28 52 4 5 $sock
Fill 28 57 4 5 $sock
Fill 28 61 4 2 $shoe

# save
$out = 'C:\Users\sasha\Desktop\femboy_furry_skin.png'
$bmp.Save($out, [System.Drawing.Imaging.ImageFormat]::Png)
$bmp.Dispose()
Write-Output "saved: $out"