# كيف تحصل على ملف APK فعلي — خطوة بخطوة

## ليه معنديش أقدر أطلّعلك APK من هنا مباشرة؟

بنيت الكود كله (95 ملف Kotlin) لكن بيئة العمل بتاعتي:
- معندهاش Android SDK ولا Gradle مثبَّتين.
- الشبكة عندي محجوبة على النطاقات اللي محتاجها لتحميلهم (`dl.google.com`, `services.gradle.org`) — جربت فعلياً وطلعتلي `403 host_not_allowed`.

يعني تحويل الكود لملف APK ثنائي (binary) مستحيل من موقعي، مش تقصير. لكن جهّزت المشروع بحيث تجيب APK حقيقي في أقل من 10 دقايق بدون ما تنزّل Android Studio حتى.

## الطريقة الأسهل: GitHub Actions (يبني APK حقيقي في السحابة تلقائياً)

1. اعمل حساب مجاني على [github.com](https://github.com) لو معندكش.
2. اعمل Repository جديد (فاضي، أي اسم، حتى Private تمام).
3. فك ضغط ملف `LayerFarmApp_Complete.zip` اللي بعتهولك، وارفع **كل المحتوى** (كل الملفات والمجلدات جوه `LayerFarmApp/`) على الـ Repository ده. أسهل طريقة:
   ```bash
   cd LayerFarmApp
   git init
   git add .
   git commit -m "أول نسخة من مزرعتي للدواجن"
   git branch -M main
   git remote add origin https://github.com/USERNAME/REPO_NAME.git
   git push -u origin main
   ```
4. روح لتبويب **Actions** في صفحة الـ Repository على GitHub. هتلاقي workflow اسمه "Build APK" شغّال تلقائياً (لأني حطيت ملف `.github/workflows/build-apk.yml` جاهز).
5. استنى 5-8 دقايق لحد ما يخلص (هتشوف علامة ✅ خضرا).
6. ادخل على الـ run اللي خلص، وهتلاقي تحت **Artifacts** ملف اسمه `layerfarmapp-debug-apk` — ده ملف APK حقيقي جاهز للتنزيل والتثبيت على أي جهاز أندرويد (فعّل "تثبيت من مصادر غير معروفة" في إعدادات الجهاز).

**ملحوظة:** أول مرة تشغّل الـ workflow وارد جداً يظهر خطأ بناء (نسخة مكتبة مش متوافقة، استيراد ناقص، إلخ) — ده متوقّع تماماً لأن الكود ده أول مرة يتصرّف بمُصرِّف حقيقي. انسخ رسالة الخطأ اللي هتظهر في الـ Actions log وابعتهالي هنا وأصلحها فوراً، وارفع كومِت جديد، والـ workflow هيشتغل تاني تلقائياً.

## الطريقة البديلة: Android Studio على جهازك

لو عندك جهاز فيه إنترنت عادي (مش محجوب زي هنا):
1. نزّل [Android Studio](https://developer.android.com/studio) (مجاني).
2. افتح المجلد `LayerFarmApp` كمشروع (Open Project).
3. Android Studio هيعمل Sync تلقائي وينزّل كل حاجة ناقصة (Gradle wrapper، SDK، المكتبات).
4. من قايمة **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
5. الملف هيبقى موجود في `app/build/outputs/apk/debug/app-debug.apk`.

كلا الطريقتين هتدّيك **نفس الملف الحقيقي**. GitHub Actions أسهل لو معندكش جهاز قوي أو نت كويس، وAndroid Studio أفضل لو عايز تعدّل وتشوف النتيجة لحظياً.
