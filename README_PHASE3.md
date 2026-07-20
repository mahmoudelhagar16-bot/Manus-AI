# المرحلة 3 — طبقة الـ Data (RepositoryImpl) + حقن التبعيات (Hilt)

## المحتوى الجديد

```
data/
 ├─ mapper/     (تحويل Entity <-> Domain Model لكل مجموعة جداول)
 └─ repository/ (تنفيذ فعلي لكل Repository Interface من المرحلة 2)

di/
 ├─ DatabaseModule.kt    (يوفّر AppDatabase وكل الـ DAOs)
 ├─ RepositoryModule.kt  (@Binds لكل Repository Interface -> Impl)
 └─ UseCaseModule.kt     (@Provides لكل UseCase، بحقن الـ Repositories المطلوبة)

app/LayerFarmApplication.kt   (@HiltAndroidApp — نقطة تفعيل Hilt)
```

## القرارات التصميمية

- **Mappers كدوال Extension نقية** (`Entity.toDomain()`, `Model.toEntity()`) — لا Manual mapping متكرر، ولا مكتبات خارجية (لا حاجة لـ MapStruct/AutoMapper في Kotlin).
- **معالجة الحالات (Enums) بأمان**: كل تحويل من String (كما هي مخزَّنة في Room) إلى Enum يستخدم `runCatching { Enum.valueOf(...) }.getOrDefault(...)` بدل استثناء صريح — يحمي من تلف بيانات محتمل بعد ترقية النسخة (قسم 14: "لا تعطُّل الواجهة أبداً بسبب خطأ بيانات").
- **DomainResult ↔ kotlin.Result**: العمليات الذرية في DAOs (`FeedConsumptionTransactionDao`, `InventoryTransactionCompositeDao`) تُرجع `kotlin.Result`، وتُحوَّل صراحة إلى `DomainResult` داخل الـ RepositoryImpl فقط — طبقة الـ Domain لا ترى Room أو kotlin.Result إطلاقاً، فقط `DomainResult` الخاص بها.
- **LocalDate ↔ epochDay(Long)**: التحويل يتم عند حدود الـ Repository فقط (`date.toEpochDay()` عند الاستدعاء، `LocalDate.ofEpochDay()` ضمنياً عبر Converters عند القراءة من Room). طبقة الـ Domain تتعامل مع `LocalDate` دائماً.
- **Hilt بدل حقن يدوي**: `DatabaseModule` يوفّر Singleton واحد لـ `AppDatabase` وكل DAO منه، و`RepositoryModule` يربط الواجهات بالتنفيذ عبر `@Binds` (أخف من `@Provides` لأنه لا يولّد كوداً إضافياً)، و`UseCaseModule` يوفّر كل UseCase (باستخدام `@Provides` لأنها classes لا interfaces).
- **نمط DAO المركّب معتمد رسمياً من توثيق Android** لكتابة عمليتين على جدولين مختلفين ضمن Transaction واحدة (`FeedConsumptionTransactionDao`, `InventoryTransactionCompositeDao`) — Room يولّد التنفيذ تلقائياً لدوال الـ DAOs الفرعية المجرَّدة.

## للدمج في مشروعك

1. انسخ محتوى `LayerFarmApp` (المراحل 1+2+3) بالكامل داخل `app/src/main/java/com/farm/layermanager/`.
2. أضف تبعيات Hilt/Room المحدَّثة من `app/build.gradle.dependencies.kts.txt` (لاحظ التغيير من `kapt` إلى `ksp`، وإضافة Hilt plugin في كل من مستوى المشروع والموديول).
3. سجّل `LayerFarmApplication` في `AndroidManifest.xml`:
   ```xml
   <application android:name=".app.LayerFarmApplication" ... >
   ```
4. Sync Gradle وابنِ المشروع.

## ما لم يُبنَ بعد

- **شاشات Compose + ViewModels** (المرحلة 4، قسم 9): تبدأ بشاشة العنابر/السلالات، ثم شاشة الإدخال اليومي (الأهم يومياً)، ثم بقية الشاشات (علف، مخزون، مبيعات، مالية، Dashboard، تقارير).
- كل ViewModel سيحقن الـ UseCases المطلوبة له مباشرة عبر `@HiltViewModel` + `@Inject constructor`.

أخبرني إذا تريد المتابعة لبناء أول شاشات Compose (العنابر والسلالات + شاشة الإدخال اليومي).
