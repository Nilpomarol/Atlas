package com.atlas.domain.util

/**
 * Static map from country/territory ISO 3166-1 alpha-2 code to its primary ISO 4217
 * currency code.
 *
 * The country stats dataset stores the currency as a localized *name* ("Yen japonès"),
 * not a code, so the currency converter needs this lookup to query an exchange-rate
 * provider. This is bundled reference data; if a future dataset version adds a
 * `currency_code` fact, that should take precedence and this map becomes the fallback.
 *
 * Only currencies supported by the rate provider will actually resolve to a live rate;
 * unsupported ones simply leave the converter hidden, so full precision here is not
 * critical for less-travelled currencies.
 */
object CountryCurrencyCodeMap {

    fun codeForCountry(iso2: String): String? = MAP[iso2.uppercase()]

    private val MAP: Map<String, String> = mapOf(
        "AF" to "AFN", "AL" to "ALL", "DZ" to "DZD", "AS" to "USD", "AD" to "EUR",
        "AO" to "AOA", "AI" to "XCD", "AG" to "XCD", "AR" to "ARS", "AM" to "AMD",
        "AW" to "AWG", "AU" to "AUD", "AT" to "EUR", "AZ" to "AZN", "BS" to "BSD",
        "BH" to "BHD", "BD" to "BDT", "BB" to "BBD", "BY" to "BYN", "BE" to "EUR",
        "BZ" to "BZD", "BJ" to "XOF", "BM" to "BMD", "BT" to "BTN", "BO" to "BOB",
        "BA" to "BAM", "BW" to "BWP", "BR" to "BRL", "BN" to "BND", "BG" to "BGN",
        "BF" to "XOF", "BI" to "BIF", "BQ" to "USD", "CV" to "CVE", "KH" to "KHR",
        "CM" to "XAF", "CA" to "CAD", "KY" to "KYD", "CF" to "XAF", "TD" to "XAF",
        "CL" to "CLP", "CN" to "CNY", "CO" to "COP", "KM" to "KMF", "CG" to "XAF",
        "CD" to "CDF", "CK" to "NZD", "CR" to "CRC", "CI" to "XOF", "HR" to "EUR",
        "CU" to "CUP", "CW" to "ANG", "CY" to "EUR", "CZ" to "CZK", "DK" to "DKK",
        "DJ" to "DJF", "DM" to "XCD", "DO" to "DOP", "EC" to "USD", "EG" to "EGP",
        "SV" to "USD", "GQ" to "XAF", "ER" to "ERN", "EE" to "EUR", "SZ" to "SZL",
        "ET" to "ETB", "FK" to "FKP", "FO" to "DKK", "FJ" to "FJD", "FI" to "EUR",
        "FR" to "EUR", "GF" to "EUR", "PF" to "XPF", "GA" to "XAF", "GM" to "GMD",
        "GE" to "GEL", "DE" to "EUR", "GH" to "GHS", "GI" to "GIP", "GR" to "EUR",
        "GL" to "DKK", "GD" to "XCD", "GP" to "EUR", "GU" to "USD", "GT" to "GTQ",
        "GG" to "GBP", "GN" to "GNF", "GW" to "XOF", "GY" to "GYD", "HT" to "HTG",
        "HN" to "HNL", "HK" to "HKD", "HU" to "HUF", "IS" to "ISK", "IN" to "INR",
        "ID" to "IDR", "IR" to "IRR", "IQ" to "IQD", "IE" to "EUR", "IM" to "GBP",
        "IL" to "ILS", "IT" to "EUR", "JM" to "JMD", "JP" to "JPY", "JE" to "GBP",
        "JO" to "JOD", "KZ" to "KZT", "KE" to "KES", "KI" to "AUD", "KP" to "KPW",
        "KR" to "KRW", "KW" to "KWD", "KG" to "KGS", "LA" to "LAK", "LV" to "EUR",
        "LB" to "LBP", "LS" to "LSL", "LR" to "LRD", "LY" to "LYD", "LI" to "CHF",
        "LT" to "EUR", "LU" to "EUR", "MO" to "MOP", "MG" to "MGA", "MW" to "MWK",
        "MY" to "MYR", "MV" to "MVR", "ML" to "XOF", "MT" to "EUR", "MH" to "USD",
        "MQ" to "EUR", "MR" to "MRU", "MU" to "MUR", "YT" to "EUR", "MX" to "MXN",
        "FM" to "USD", "MD" to "MDL", "MC" to "EUR", "MN" to "MNT", "ME" to "EUR",
        "MS" to "XCD", "MA" to "MAD", "MZ" to "MZN", "MM" to "MMK", "NA" to "NAD",
        "NR" to "AUD", "NP" to "NPR", "NL" to "EUR", "NC" to "XPF", "NZ" to "NZD",
        "NI" to "NIO", "NE" to "XOF", "NG" to "NGN", "NU" to "NZD", "NF" to "AUD",
        "MK" to "MKD", "MP" to "USD", "NO" to "NOK", "OM" to "OMR", "PK" to "PKR",
        "PW" to "USD", "PS" to "ILS", "PA" to "PAB", "PG" to "PGK", "PY" to "PYG",
        "PE" to "PEN", "PH" to "PHP", "PN" to "NZD", "PL" to "PLN", "PT" to "EUR",
        "PR" to "USD", "QA" to "QAR", "RE" to "EUR", "RO" to "RON", "RU" to "RUB",
        "RW" to "RWF", "BL" to "EUR", "SH" to "SHP", "KN" to "XCD", "LC" to "XCD",
        "MF" to "EUR", "PM" to "EUR", "VC" to "XCD", "WS" to "WST", "SM" to "EUR",
        "ST" to "STN", "SA" to "SAR", "SN" to "XOF", "RS" to "RSD", "SC" to "SCR",
        "SL" to "SLE", "SG" to "SGD", "SX" to "ANG", "SK" to "EUR", "SI" to "EUR",
        "SB" to "SBD", "SO" to "SOS", "ZA" to "ZAR", "SS" to "SSP", "ES" to "EUR",
        "LK" to "LKR", "SD" to "SDG", "SR" to "SRD", "SJ" to "NOK", "SE" to "SEK",
        "CH" to "CHF", "SY" to "SYP", "TW" to "TWD", "TJ" to "TJS", "TZ" to "TZS",
        "TH" to "THB", "TL" to "USD", "TG" to "XOF", "TK" to "NZD", "TO" to "TOP",
        "TT" to "TTD", "TN" to "TND", "TR" to "TRY", "TM" to "TMT", "TC" to "USD",
        "TV" to "AUD", "UG" to "UGX", "UA" to "UAH", "AE" to "AED", "GB" to "GBP",
        "US" to "USD", "UY" to "UYU", "UZ" to "UZS", "VU" to "VUV", "VA" to "EUR",
        "VE" to "VES", "VN" to "VND", "VG" to "USD", "VI" to "USD", "WF" to "XPF",
        "EH" to "MAD", "YE" to "YER", "ZM" to "ZMW", "ZW" to "ZWL", "XK" to "EUR",
    )
}
