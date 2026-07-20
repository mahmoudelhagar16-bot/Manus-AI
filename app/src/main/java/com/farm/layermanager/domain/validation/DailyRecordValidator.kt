package com.farm.layermanager.domain.validation

import com.farm.layermanager.domain.model.DailyRecord

/**
 * كل قواعد UC-05 (التحقق التلقائي قبل الحفظ) مجمّعة هنا بمعزل عن الـ UseCase،
 * لتسهيل اختبارها وحدوياً دون الحاجة لقاعدة بيانات.
 */
object DailyRecordValidator {

    /**
     * @param previousLiveBirds عدد الطيور الحية في آخر سجل سابق لنفس (عنبر × سلالة)، أو null إن كان هذا أول سجل
     *        (وعندها يُقارَن مقابل initialChickCount للسلالة).
     * @param referenceBirdsForFirstRecord initialChickCount السلالة، يُستخدم فقط عند previousLiveBirds == null
     */
    fun validate(
        record: DailyRecord,
        previousLiveBirds: Int?,
        referenceBirdsForFirstRecord: Int,
        eggsPerTray: Int = 30
    ): List<String> {
        val errors = mutableListOf<String>()
        val baseline = previousLiveBirds ?: referenceBirdsForFirstRecord

        if (record.mortality < 0) errors += "عدد النافق لا يمكن أن يكون بالسالب"
        if (record.culled < 0) errors += "عدد المستبعد لا يمكن أن يكون بالسالب"
        if (record.liveBirds < 0) errors += "عدد الطيور الحية لا يمكن أن يكون بالسالب"
        if (record.feedQtyKg < 0) errors += "كمية العلف لا يمكن أن تكون بالسالب"
        if (record.waterLiters < 0) errors += "كمية المياه لا يمكن أن تكون بالسالب"
        if (record.productionTrays < 0) errors += "عدد أطباق الإنتاج لا يمكن أن يكون بالسالب"

        record.lightHours?.let {
            if (it < 0.0 || it > 24.0) errors += "عدد ساعات الإضاءة يجب أن يكون بين 0 و 24"
        }

        // القاعدة الأساسية: نافق + مستبعد اليوم ≤ عدد الطيور الحية في السجل السابق (أو العدد الابتدائي لأول سجل)
        if (record.mortality + record.culled > baseline) {
            errors += "مجموع (النافق + المستبعد = ${record.mortality + record.culled}) أكبر من عدد الطيور المتاح ($baseline)"
        }

        // السقف المنطقي: عدد البيض المنتج لا يتجاوز عدد الطيور الحية المُدخلة في نفس السجل
        val producedEggs = record.productionTrays * eggsPerTray
        if (producedEggs > record.liveBirds) {
            errors += "عدد البيض المنتج (${producedEggs.toInt()}) أكبر من عدد الطيور الحية المُدخل (${record.liveBirds})"
        }

        // اتساق داخلي: liveBirds المُدخل يجب أن يساوي منطقياً (baseline − mortality − culled)
        val expectedLiveBirds = baseline - record.mortality - record.culled
        if (record.liveBirds != expectedLiveBirds) {
            errors += "عدد الطيور الحية المُدخل (${record.liveBirds}) لا يطابق الحساب المتوقع ($expectedLiveBirds = $baseline − ${record.mortality} − ${record.culled})"
        }

        return errors
    }
}
