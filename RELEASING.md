# راهنمای انتشار نسخه (Release)

## یک‌بار: ساخت کلید امضا
این کلید هویت برنامه است. اگر گم شود یا عوض شود، نسخه‌های بعدی روی نسخه نصب‌شده نصب نمی‌شوند. **از آن در جای امن نسخه پشتیبان بگیرید و هرگز در مخزن قرار ندهید.**

```bash
keytool -genkeypair -v \
  -keystore zarb-release.keystore \
  -alias zarb -keyalg RSA -keysize 2048 -validity 10000
```

سپس فایل را base64 کنید:

```bash
base64 -w0 zarb-release.keystore > keystore.b64     # لینوکس
# macOS: base64 -i zarb-release.keystore -o keystore.b64
```

## یک‌بار: ثبت Secrets در گیت‌هاب
مسیر: Settings > Secrets and variables > Actions > New repository secret

- `KEYSTORE_BASE64` : محتوای فایل `keystore.b64`
- `KEYSTORE_PASSWORD` : رمز keystore
- `KEY_ALIAS` : `zarb` (یا alias انتخابی شما)
- `KEY_PASSWORD` : رمز کلید

## هر نسخه جدید
1. در `app/build.gradle.kts` هر دو مقدار را افزایش دهید: `versionCode` (عدد صحیح بزرگ‌تر از قبلی) و `versionName`.
2. تغییرات را کامیت و push کنید و منتظر سبز شدن CI بمانید.
3. تگ را دقیقاً هم‌نام `versionName` بزنید:
   ```bash
   git tag v1.4.0
   git push origin v1.4.0
   ```
4. CI اگر تگ با `versionName` یکی نباشد یا کلید امضا نباشد، متوقف می‌شود. در حالت موفق، APK امضاشده در Releases منتشر می‌شود.

## نکات
- شناسه برنامه انتشار `com.zarb.hero` است. نسخه‌های debug قدیمی با شناسه `com.zarb.hero.debug` نصب می‌شدند؛ این دو برنامه جدا هستند و سوابق از نسخه debug به نسخه انتشار منتقل نمی‌شود.
- بعد از اولین بیلد موفق، artifact با نام `room-schemas` را از Actions دانلود کنید و پوشه `app/schemas/` را در مخزن کامیت کنید (برای نوشتن Migration در آینده).
- بیلد روی PR یا شاخه main بدون Secrets با کلید موقت debug امضا می‌شود و برای توزیع نیست.
