# المرحلة 1 — طبقة قاعدة البيانات (Room)

هذه أول مرحلة من خطة التنفيذ (قسم 9 في وثيقة التحليل): إعداد Room Database كاملاً
(Entities + DAOs + Views المشتقة + Migrations)، وفق Clean Architecture ثلاثية الطبقات.

## المحتوى

```
app/src/main/java/com/farm/layermanager/data/local/
 ├─ entity/
 │   ├─ HouseEntity.kt
 │   ├─ StrainEntity.kt
 │   ├─ DailyRecordEntity.kt
 │   ├─ HealthEntities.kt          (Vaccination + Medication)
 │   ├─ FeedEntities.kt            (FeedType + FeedConsumption)
 │   ├─ InventoryEntities.kt       (InventoryItem + InventoryTransaction)
 │   ├─ SalesEntities.kt           (Customer + Sale)
 │   ├─ FinanceEntities.kt         (ExpenseCategory + Expense + RevenueType + Revenue)
 │   ├─ SchemaMetadataEntity.kt
 │   └─ DatabaseViews.kt           (egg_inventory_balance + strain_cumulative_stats)
 ├─ dao/                            (DAO مقابل لكل كيان + DAOs مركّبة للعمليات الذرية)
 ├─ converter/Converters.kt         (LocalDate <-> epochDay)
 └─ database/
     ├─ AppDatabase.kt
     └─ AppDatabaseMigrations.kt
```

## قرارات تصميمية مهمة نُفِّذت فعلياً

- **لا قيم مشتقة مخزَّنة**: العمر، نسبة البقاء، النافق التراكمي كلها Views/محسوبة، ما عدا
  `weightedAvgCost` في `feed_types` (استثناء مبرَّر: تكلفة تاريخية مجمَّدة).
- **RESTRICT على كل العلاقات الحرجة محاسبياً** (`ForeignKey.RESTRICT`) يمنع حذف عنبر/سلالة له سجلات.
- **UNIQUE(recordDate, houseId, strainId)** يمنع تكرار سجل نفس اليوم.
- **عمليات ذرية (Transaction)**:
  - `FeedConsumptionTransactionDao.recordConsumption` = خصم من مخزون العلف + إدراج سجل الاستهلاك، معاً أو لا شيء.
  - `FeedConsumptionTransactionDao.recordFeedPurchase` = تحديث المتوسط المرجّح للتكلفة عند كل وارد جديد.
  - `InventoryTransactionCompositeDao.recordTransaction` = تحديث رصيد المخزون العام + تسجيل الحركة، مع رفض أي صادر يجعل الرصيد سالباً.
- **Seed تلقائي** عند أول تشغيل: فئات المصروفات (11 فئة) وأنواع الإيرادات الافتراضية (قسم 5.9/5.10).
- **Migrations جاهزة للتوسع**: `AppDatabaseMigrations.ALL` فارغة الآن (version=1)، وبها تعليمات وأمثلة لإضافة أي ترحيل مستقبلي دون فقد بيانات.
- **`java.time.LocalDate`** بدلاً من String للتواريخ، مخزَّنة كـ `epochDay` (Long) لأداء أفضل في الفهرسة والفرز — يتطلب تفعيل `coreLibraryDesugaring` (موجود في ملف الـ Gradle المرفق).

## كيفية الدمج في مشروعك

1. انسخ مجلد `app/src/main/java/com/farm/layermanager/data/local` كاملاً داخل مشروع Android Studio الخاص بك بنفس المسار (`app/src/main/java/com/farm/layermanager/...`).
2. أضف التبعيات الموجودة في `app/build.gradle.dependencies.kts.txt` إلى ملف `app/build.gradle.kts` الفعلي لديك.
3. Sync Gradle، ثم ابنِ المشروع — يجب أن يتم توليد كود Room دون أخطاء.

## ما لم يُبنَ بعد (الخطوة التالية حسب قسم 9)

- **المرحلة 2**: Domain Layer كاملة — Models + UseCases (تحقق الإدخال، الحسابات) + `CalculationEngine` (كل معادلات قسم 12 كدوال نقية قابلة للاختبار).
- بعدها: Repository Interfaces + RepositoryImpl (تربط Domain بـ Room) + Hilt DI Modules.

أخبرني إذا تريد المتابعة مباشرة للمرحلة 2 (Domain + UseCases + CalculationEngine)، أم تفضّل مراجعة هذه المرحلة أولاً.
