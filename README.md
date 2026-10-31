# Revision — متصفح أندرويد خاص / Private Android Browser

> **Revision** is a secure-by-default, WebView-based Android browser with a
> liquid-glass, sci-fi UI and a unique long-press drag feature.

---

## العربية

### ما هو Revision؟
متصفح أندرويد مبني على WebView، مصمّم ليكون خاصاً بقدر الإمكان:
- **لا يطلب أذونات الموقع أو الكاميرا أو الميكروفون أبداً**، ويرفض طلباتها من المواقع تلقائياً.
- **HTTPS فقط:** الروابط `http://` تُحوَّل إلى `https://`، وإعداد أمان الشبكة يمنع حركة النص الواضح.
- **حظر متتبّعات:** طلبات نطاقات إعلانات/تتبّع معروفة تُعترض وتُمنع قبل اكتمالها.
- **كوكيز الطرف الثالث معطّلة،** ورأس `DNT: 1` (عدم التتبّع) يُرسل مع التصفح.
- **وضع خاص افتراضياً:** التطبيق نفسه لا يحفظ سجلّ الزيارات ولا التبويبات على القرص.
- **مسح البيانات:** زر مسح يزيل الكوكيز والكاش والسجلّ وبيانات WebStorage، وخيار "مسح عند الخروج".

### الهوية البصرية — زجاج سائل وخيال علمي
لوحات شفافة بحدود متوهّجة سماوية/بنفسجية، خلفية فضاء داكنة، وحركات دخول
ونبض لزر الدرع وتقدّم تحميل متحرك.

### الميزة الفريدة — السحب بالضغط المطوّل
اضغط مطوّلاً (~نصف ثانية) على الشريط العلوي (شريط العنوان/البحث) أو على
لوحة الإجراءات السفلية، ثم اسحبها مثل أيقونة الشاشة الرئيسية. عند الإفلات
يُحفظ الموضع في `SharedPreferences` ويُستعاد عند إعادة فتح التطبيق.
زر "Reset layout" يعيد اللوحات إلى مواضعها الأصلية.

### حدود الصدق — اقرأ هذا قبل أن تثق ثقة عمياء
هذا التطبيق **ليس "غير قابل للاختراق" ولا يمنع كل التسريب**:
- قائمة حظر المتتبّعات محدودة، ونطاقات جديدة تظهر باستمرار.
- WebView لا يمنع بصمة المتصفح (fingerprinting) بالكامل، ولا يخفي عنوان IP الخاص بك.
- التطبيق ليس VPN ولا Tor، ولا يحميك إذا ثبّتّ تطبيقاً خبيثاً آخر أو أدخلت بياناتك في موقع تصيّد.
- بعض المواقع التي تعمل بـ `http://` فقط لن تعمل، وهذا مقصود لأجل الأمان.

### البناء محلياً
```bash
# المتطلبات: JDK 17 + Android SDK (platform 34, build-tools 34)
export ANDROID_HOME=$HOME/Android/Sdk
./gradlew assembleDebug
# الناتج: app/build/outputs/apk/debug/app-debug.apk
```

### ملاحظة عن البناء المحلي في بيئة Muse الحالية
في بيئة التطوير الحالية، فشل البناء المحلي بـ Gradle ليس بسبب الكود، بل
لأن Gradle يحتاج إلى تواصل TCP محلي بين العميل والـ daemon، وهذا التواصل
محظور في هذه البيئة (رسالة الاختبار: اتصالات TCP الأخرى معطّلة لهذا
المساعد). لذلك الطريقة الموثوقة حالياً هي البناء عبر GitHub Actions أدناه.

### البناء عبر GitHub Actions
ادفع الكود إلى مستودع، وسيبني سير العمل في `.github/workflows/android.yml`
ملف APK ويرفعه كـ Artifact باسم `revision-debug-apk`.

---

## English

### What is Revision?
A private-by-default, WebView-based Android browser:

- **Never requests location, camera, or microphone permissions**, and denies
  those requests from websites automatically.
- **HTTPS only:** `http://` URLs are upgraded to `https://`, and the network
  security config forbids cleartext traffic.
- **Tracker blocking:** known ad/tracker domains are intercepted in
  `shouldInterceptRequest` and never complete.
- **Third-party cookies off**, `DNT: 1` header sent, Safe Browsing on,
  mixed content never allowed, file access off.
- **Private by default:** Revision itself never writes visited URLs or tabs
  to disk — tabs and history live only in memory.
- **Clear data button + optional clear-on-exit** (cookies, cache, history,
  WebStorage).

### Visual identity — liquid glass, sci-fi
Translucent panels with glowing cyan/violet strokes, a dark space
background, entrance animations, a pulsing shield toggle, and an animated
load-progress bar.

### Unique feature — long-press drag
Long-press (~450 ms) the top address/search bar or the bottom actions panel,
then drag it like a home-screen icon. On release the position is saved in
`SharedPreferences` and restored on next launch. "Reset layout" restores
the original positions.

### Honest limits
Revision is **not "unhackable" and does not stop all leaks**:
- The tracker blocklist is curated and finite; new tracker domains appear constantly.
- WebView does not fully prevent browser fingerprinting, and does not hide your IP.
- This is not a VPN or Tor, and it cannot protect you from a malicious app
  on the device or from entering credentials on a phishing site.
- HTTP-only sites will not work — that is intentional.

### Build locally
```bash
# Requirements: JDK 17 + Android SDK (platform 34, build-tools 34)
export ANDROID_HOME=$HOME/Android/Sdk
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Build on GitHub Actions
Push to a repository and the workflow in `.github/workflows/android.yml`
builds the APK and uploads it as the `revision-debug-apk` artifact.

---

## Project structure

```
app/src/main/java/com/revision/browser/
  MainActivity.kt          # Browser, tabs, privacy wiring, animations
  DraggablePanelLayout.kt  # Long-press drag + position persistence
  TrackerBlocker.kt        # Known tracker/ad domain blocklist
  RevisionApp.kt           # App-level privacy defaults
app/src/main/res/layout/activity_main.xml
app/src/main/res/drawable/ # glass_panel, glass_button, bg_space
.github/workflows/android.yml
```

## Next step for GitHub (not done yet)
The GitHub API repo creation failed with 403 ("Resource not accessible by
integration"), so no repo was created and nothing was pushed. To finish:
1. The user creates an **empty private repo named `Revision`** on GitHub.
2. The user installs the **Muse GitHub App** on that repo.
3. Then the local project can be pushed to it.
