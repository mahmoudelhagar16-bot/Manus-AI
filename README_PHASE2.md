# المرحلة 2 — طبقة الـ Domain (Models + UseCases + CalculationEngine)

## المحتوى

```
domain/
 ├─ common/DomainResult.kt        (غلاف نتائج موحّد + Validator عام)
 ├─ model/                        (نماذج نظيفة، لا تعرف Room إطلاقاً)
 ├─ calculation/CalculationEngine.kt   (كل معادلات قسم 12 كدوال نقية)
 ├─ validation/DailyRecordValidator.kt (منطق UC-05 معزول وقابل للاختبار)
 ├─ repository/                   (Interfaces فقط — التنفيذ الفعلي يأتي في المرحلة القادمة)
 └─ usecase/
     ├─ house/       AddHouse, UpdateHouse, (De)ActivateHouse, GetHouses
     ├─ strain/       AddStrain, UpdateStrain, DeactivateStrain, GetStrainDetails
     ├─ dailyrecord/  AddDailyRecord (UC-05), UpdateDailyRecord (UC-06), DeleteDailyRecord, GetDailyRecords
     ├─ health/       AddVaccination, AddMedication + Get
     ├─ feed/         AddFeedType, AddFeedPurchase, RecordFeedConsumption (UC-10), GetFeed
     ├─ inventory/    AddInventoryItem, RecordInventoryTransaction (UC-11), GetInventory
     ├─ sales/        AddCustomer, RecordSale (UC-13/UC-14), GetSales, GetCustomers
     ├─ finance/       AddExpense, AddRevenue, AllocateGeneralExpense, GetNetProfit (UC-17)
     └─ reports/       GetDashboard (UC-18), GenerateReport (UC-19), SearchAll (UC-20)
```

## أهم القرارات المنفَّذة فعلياً

- **DomainResult بدل Exceptions**: كل UseCase يُرجع `DomainResult.Success`/`Error` برسالة عربية واضحة، بدل رمي استثناءات — الواجهة تعرض `error.message` مباشرة.
- **AddDailyRecordUseCase / UpdateDailyRecordUseCase**: ينفّذان UC-05 بالكامل عبر `DailyRecordValidator`، ويتحققان من:
  - عدم تكرار سجل نفس اليوم (بحث مسبق قبل الإدراج، ليس فقط الاعتماد على UNIQUE constraint).
  - `mortality + culled ≤ liveBirds اليوم السابق` (أو `initialChickCount` لأول سجل).
  - اتساق `liveBirds` المُدخل مع الحساب المتوقع.
  - سقف `productionTrays × 30 ≤ liveBirds`.
- **RecordFeedConsumptionUseCase / RecordSaleUseCase**: يتحققان من الرصيد المتاح *قبل* الوصول لقاعدة البيانات (رسالة فورية)، بينما التحقق النهائي الذري يبقى في الـ Repository/DAO كخط دفاع أخير (نفس النمط المستخدم في المرحلة 1).
- **CalculationEngine**: 15 دالة نقية تقابل كل صف في جدول معادلات قسم 12 حرفياً (العمر، البقاء، Hen-Day، FCR، التكاليف، توزيع المصروف العام...) — قابلة للاختبار الوحدوي فوراً بدون Mock لأي شيء.
- **AllocateGeneralExpenseUseCase**: يحسب فعلياً عدد طيور كل عنبر من `strains × strain_cumulative_stats` (وليس Placeholder)، ويُرجع `null` عند القسمة على صفر بدل رمي خطأ (قسم 14).
- **GetDashboardUseCase**: يدمج 10 مصادر Flow حية (عنابر نشطة، إحصاءات تراكمية، سجلات اليوم/الأسبوع/الشهر، مبيعات، مصروفات، إيرادات، مديونية) في `DashboardSnapshot` واحدة تتحدّث لحظياً، مقسَّمة لمرحلتين داخلياً لضمان type-safety الكاملة مع `combine`.
- **GenerateReportUseCase**: يدعم 3 نطاقات (عنبر×سلالة / عنبر كامل / المزرعة كاملة) حسب UC-19، مع توثيق صريح للفرق بين دقة النطاقين.

## ما لم يُبنَ بعد (المرحلة القادمة)

- **RepositoryImpl**: تنفيذ فعلي لكل Repository Interface يربط Room DAOs (من المرحلة 1) بنماذج الـ Domain عبر Mappers (Entity ↔ Model).
- **Hilt DI Modules**: لحقن Repositories وUseCases تلقائياً.
- بعدها: شاشات Compose (تبدأ بالعنابر/السلالات ثم شاشة الإدخال اليومي، حسب قسم 9).

أخبرني إذا تريد المتابعة لطبقة الـ Data (RepositoryImpl + Mappers + Hilt) لربط كل هذا بقاعدة بيانات المرحلة 1 فعلياً.
