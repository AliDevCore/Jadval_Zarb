# راهنمای انتشار نسخه (Release)

## کلید امضا و حفظ اطلاعات قبلی کاربران
این برنامه با کلید اختصاصی `zarb-release.keystore` امضا می‌شود.
> [!IMPORTANT]
> **این کلید هویت رسمی برنامه روی گوشی کاربران است.**
> برای اینکه نسخه‌های جدید بدون پاک شدن سوابق، نتایج، آزمون‌های ذخیره‌شده و دیتابیس Room روی نسخه قبلی کاربر نصب شوند (Update):
> ۱. برنامه باید همیشه با همین کلید اصلی (`zarb-release.keystore`) امضا شود.
> ۲. شناسه برنامه `com.zarb.hero` باقی بماند.
> ۳. در هر نسخه جدید، مقدار `versionCode` در `app/build.gradle.kts` حتماً یک عدد بزرگتر از نسخه قبلی باشد.

پوشه کلید در مسیر زیر قرار دارد و در `.gitignore` قرار داده شده تا در گیت کامیت نشود:
`Jadval Zarb Key/zarb-release.keystore`

---

## متغیرهای Secrets در گیت‌هاب (یک‌بار تنظیم)
در مسیر ریپازیتوری گیت‌هاب:
`Settings > Secrets and variables > Actions > Repository secrets`

چهار سکرت زیر در گیت‌هاب تنظیم شده‌اند:
- `KEYSTORE_BASE64` (یا `RELEASE_KEYSTORE_BASE64`): محتوای فایل `keystore.b64`
- `KEYSTORE_PASSWORD` (یا `RELEASE_KEYSTORE_PASSWORD`): رمز keystore
- `KEY_ALIAS` (یا `RELEASE_KEY_ALIAS`): `zarb`
- `KEY_PASSWORD` (یا `RELEASE_KEY_PASSWORD`): رمز کلید

---

## روش‌های انتشار نسخه جدید در گیت‌هاب

### روش ۱: انتشار خودکار با Push به شاخه main (ساده‌ترین روش - با GitHub Desktop)
1. در فایل `app/build.gradle.kts`، مقدار `versionCode` را افزایش دهید (مثلاً ۷) و `versionName` را به‌روز کنید (مثلاً `1.1.0`).
2. در **GitHub Desktop** تغییرات را Commit و سپس `Push origin` کنید.
3. گیت‌هاب به‌طور خودکار:
   - تست‌های برنامه را اجرا می‌کند.
   - هر دو فایل APK (نسخه Release امضاشده با کلید اصلی + نسخه Debug) را کامپایل می‌کند.
   - امضای APK را اعتبارسنجی می‌کند.
   - یک Release رسمی جدید در بخش **Releases** گیت‌هاب ایجاد کرده و هر دو فایل را ضمیمه آن می‌کند.

### روش ۲: انتشار دستی با دکمه Run workflow در تب Actions
1. وارد تب **Actions** ریپازیتوری در گیت‌هاب شوید.
2. روی گردش‌کار **Build, Test & Release Android APK** کلیک کنید.
3. دکمه **Run workflow** را بزنید (اختیاری: می‌توانید تگ یا عنوان دلخواه نیز وارد کنید).

### روش ۳: انتشار با تگ گیت
```bash
git tag v1.1.0
git push origin v1.1.0
```
