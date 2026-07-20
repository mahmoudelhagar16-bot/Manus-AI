package com.farm.layermanager.domain.common

/**
 * غلاف موحّد لنتائج كل الـ UseCases بدلاً من الاعتماد على Exceptions للتحكم بتدفق الواجهة.
 * الواجهة (ViewModel) تقرأ [DomainResult.Error.message] مباشرة لعرضها للمستخدم (UC-05: التحقق التلقائي).
 */
sealed class DomainResult<out T> {
    data class Success<T>(val data: T) : DomainResult<T>()
    data class Error(val message: String) : DomainResult<Nothing>()

    inline fun onSuccess(action: (T) -> Unit): DomainResult<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (String) -> Unit): DomainResult<T> {
        if (this is Error) action(message)
        return this
    }

    fun getOrNull(): T? = (this as? Success)?.data
}

/** قواعد تحقق عامة قابلة لإعادة الاستخدام عبر كل الـ UseCases (UC-05). */
object Validator {

    fun requireNotBlank(value: String, fieldName: String): String? =
        if (value.isBlank()) "حقل \"$fieldName\" لا يمكن أن يكون فارغاً" else null

    fun requirePositive(value: Number, fieldName: String): String? =
        if (value.toDouble() <= 0.0) "حقل \"$fieldName\" يجب أن يكون أكبر من صفر" else null

    fun requireNonNegative(value: Number, fieldName: String): String? =
        if (value.toDouble() < 0.0) "حقل \"$fieldName\" لا يمكن أن يكون بالسالب" else null

    fun requireInRange(value: Double, min: Double, max: Double, fieldName: String): String? =
        if (value < min || value > max) "حقل \"$fieldName\" يجب أن يكون بين $min و $max" else null

    fun requireLessOrEqual(value: Double, max: Double, fieldName: String, maxLabel: String): String? =
        if (value > max) "حقل \"$fieldName\" ($value) أكبر من $maxLabel المتاح ($max)" else null
}
