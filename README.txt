طالع‌بینی اوس مهدی
====================

ساخت با:
- Java 17
- Android Gradle Plugin 9.0.1
- Gradle 9.7.1
- compileSdk 33
- minSdk 23

ساخت APK در Termux:
1) وارد پوشه پروژه شو:
   cd TalaBini

2) مطمئن شو Java 17 فعال است:
   export JAVA_HOME=$PREFIX/opt/openjdk-17

3) ساخت:
   gradle assembleDebug

4) APK:
   app/build/outputs/apk/debug/app-debug.apk

تغییر متن‌ها:
فایل‌های داخل app/src/main/assets را ویرایش کن:
- description.txt
- fate.txt
- best_death.txt
- worst_death.txt
- advice.txt

هر گزینه را با کاما انگلیسی جدا کن:
گزینه اول,گزینه دوم,گزینه سوم

هر بار که دکمه زده شود، از بین گزینه‌ها یکی به صورت تصادفی انتخاب می‌شود.

ثروت:
از ۰ تا ۱۰۰,۰۰۰,۰۰۰,۰۰۰,۰۰۰ تومان
با گام دقیق ۱۰۰۰ تومان.
بنابراین ۱۵۰۰، ۲۵۰۰ و ... هیچ‌وقت تولید نمی‌شوند.
