package com.example.assistant

object NyraSafetyPolicy {

    const val FINANCIAL_SAFETY_WARNING = "Boss, safety policies prevent me from carrying out financial, banking, or password transactions."
    const val ADMIN_EMAIL = "vivekgoswamirk@gmail.com"

    private val FINANCIAL_KEYWORDS = listOf(
        "pay", "payment", "upi", "gpay", "google pay", "phonepe", "paytm",
        "bank", "banking", "transfer", "money", "send money", "send cash",
        "otp", "read otp", "password", "pin", "cvv", "credit card", "debit card",
        "atm", "account balance", "balance check", "buy", "purchase", "checkout",
        "transaction", "wallet", "crypto", "bitcoin"
    )

    private val ABUSIVE_KEYWORDS = listOf(
        "abuse", "gaali", "gali", "madarchod", "bhenchod", "bhosdike", "chutiya",
        "harami", "bhadwe", "fuck", "bitch", "bastard", "asshole", "stupid", "dog", "kamina",
        "sale", "saale", "scoundrel", "idiot", "nonsense", "bloody"
    )

    fun isFinancialOrRestrictedOperation(query: String): Boolean {
        val lower = query.lowercase().trim()
        return FINANCIAL_KEYWORDS.any { keyword ->
            if (keyword.length <= 4) {
                // Use word boundary check for short words like "pay", "buy", "pin", "atm"
                Regex("\\b${Regex.escape(keyword)}\\b").containsMatchIn(lower)
            } else {
                lower.contains(keyword)
            }
        }
    }

    fun containsAbuse(query: String): Boolean {
        val lower = query.lowercase().trim()
        return ABUSIVE_KEYWORDS.any { keyword ->
            lower.contains(keyword)
        }
    }

    fun isAdmin(email: String): Boolean {
        return email.lowercase().trim() == ADMIN_EMAIL
    }
}

