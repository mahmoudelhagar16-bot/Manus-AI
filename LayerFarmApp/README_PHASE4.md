# المرحلة 4 — واجهات Compose + ViewModels (الجزء الأول)

## المحتوى الجديد

```
ui/
 ├─ theme/         Color.kt, Type.kt, Theme.kt   (هوية بصرية مستقلة عن قوالب Material الافتراضية)
 ├─ common/        CommonComponents.kt            (StatCard, NumericField, ErrorMessageCard, FullScreenLoading)
 ├─ navigation/    Screen.kt, LayerFarmNavHost.kt
 └─ screens/
     ├─ house/     HouseListViewModel+Screen, StrainListViewModel+Screen
     └─ dailyrecord/ DailyRecordViewModel+Screen  ← أهم شاشة تشغيلية يومياً

app/MainActivity.kt                (نقطة الدخول، Hilt + Compose)
app/src/main/AndroidManifest.xml.example  (مثال جاهز للدمج)
```

## الهوية البصرية (قسم "التصميم")

بدل الألوان الافتراضية العشوائية، اخترتُ لوحة مبنية على عناصر حقيقية من مزرعة دواجن بياضة:
- **زيتوني غامق** (`#3D4A2A`) = الحقول/الحظائر → اللون الأساسي (أزرار، عناصر تفاعلية).
- **عنبر صفار البيض** (`#E8A33D`) = المنتج نفسه → لون ثانوي/تمييز (Chips، أرقام بارزة).
- **أحمر الحظيرة** (`#B3453B`) = لون حظائر المزارع التقليدي → **حصرياً** لرسائل الخطأ/التنبيه (قسم UC-05)، بحيث يتعلّم المستخدم بصرياً أن هذا اللون = "شيء يحتاج انتباهاً".
- **بيج قش دافئ** للخلفيات، وتباعد أوسع للأرقام الكبيرة (`StatNumberStyle`) لتقليل خطأ القراءة الميدانية (شمس/نظارات).
- **RTL مفروض دائماً** (`CompositionLocalProvider(LocalLayoutDirection provides Rtl)`) لأن كل محتوى التطبيق عربي بغض النظر عن لغة نظام الجهاز.
- `dynamicColor` (Material You) **مُعطَّل عمداً** — موثَّق السبب في الكود: نحتاج تمييزاً بصرياً ثابتاً لحالات الخطأ عبر كل الأجهزة، لا تكيّفاً مع خلفية شاشة المستخدم.

## أهم قرار وظيفي: شاشة الإدخال اليومي

- تعرض **"عدد الطيور الحية المرجعي"** (baseline) في بطاقة بارزة أعلى الشاشة *قبل* أي إدخال — يحسبه `currentBaseline()` تلقائياً (آخر `liveBirds` مسجَّل، أو `initialChickCount` لأول سجل)، فلا يحتاج المزارع لحساب الرقم يدوياً كل يوم.
- `liveBirds` **لا يُدخله المستخدم يدوياً** — يُحسب تلقائياً (`baseline - mortality - culled`) في الـ ViewModel قبل الإرسال لـ `AddDailyRecordUseCase`، هذا يمنع بنيوياً أكثر خطأ إدخال شائع (تعارض الأرقام) بدل الاعتماد على رسالة تحقق بعدية فقط.
- عند نجاح الحفظ (`savedSuccessfully`) تُغلق الشاشة تلقائياً عبر `LaunchedEffect` وتعود لقائمة السلالات.
- كل رسائل `DomainResult.Error` (قد تحتوي عدة أسطر من `DailyRecordValidator`) تُعرض كاملة عبر `ErrorMessageCard` كنقاط منفصلة، وليس سطراً واحداً مدمجاً.

## للدمج في مشروعك

1. انسخ محتوى `LayerFarmApp` (المراحل 1+2+3+4) داخل `app/src/main/java/com/farm/layermanager/`.
2. أضف تبعيات Compose/Navigation/Hilt المحدَّثة من `app/build.gradle.dependencies.kts.txt`.
3. استبدل/ادمج `AndroidManifest.xml` الفعلي بمحتوى `AndroidManifest.xml.example` (سجّل `LayerFarmApplication` و`MainActivity`).
4. أضف `Theme.LayerFarmApp` بسيط في `res/values/themes.xml` (أي ثيم `Theme.Material3.DayNight.NoActionBar` يكفي كأساس، لأن Compose يتولى الباقي).
5. Sync Gradle وشغّل التطبيق — نقطة البداية شاشة العنابر.

## ما لم يُبنَ بعد

شاشات Compose المتبقية (كلها تتبع نفس نمط ViewModel+Screen المستخدم هنا):
- **العلف**: إضافة نوع/وارد، تسجيل استهلاك يومي (UC-09/UC-10).
- **المخزون العام**: أصناف وحركات (UC-11).
- **المبيعات**: عملاء، تسجيل بيع مع عرض رصيد المخزون المتاح مباشرة (UC-13/UC-14).
- **المالية**: مصروفات، إيرادات، صافي الربح (UC-15/16/17).
- **لوحة التحكم (Dashboard)**: تستهلك `GetDashboardUseCase` جاهزة من المرحلة 2 (UC-18).
- **التقارير**: تستهلك `GenerateReportUseCase` جاهزة (UC-19).
- شريط تنقّل سفلي (Bottom Navigation) يربط كل هذه الشاشات ببعضها بدل التنقل الخطي الحالي فقط.

أخبرني إذا تريد المتابعة لبناء الشاشات المتبقية (علف/مخزون/مبيعات/مالية/Dashboard) دفعة واحدة أو بالترتيب.
