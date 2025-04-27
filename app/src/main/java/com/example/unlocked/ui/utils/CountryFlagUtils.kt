package com.example.unlocked.ui.utils

import java.util.Locale

object CountryFlagUtils {
    private val countryNameToISO = mapOf(
        // Europe
        "albania" to "AL", "andorra" to "AD", "austria" to "AT", "belarus" to "BY",
        "belgium" to "BE", "bosnia and herzegovina" to "BA", "bulgaria" to "BG",
        "croatia" to "HR", "cyprus" to "CY", "czech republic" to "CZ", "czechia" to "CZ",
        "denmark" to "DK", "estonia" to "EE", "finland" to "FI", "france" to "FR",
        "germany" to "DE", "greece" to "GR", "hungary" to "HU", "iceland" to "IS",
        "ireland" to "IE", "italy" to "IT", "kosovo" to "XK", "latvia" to "LV",
        "liechtenstein" to "LI", "lithuania" to "LT", "luxembourg" to "LU",
        "malta" to "MT", "moldova" to "MD", "monaco" to "MC", "montenegro" to "ME",
        "netherlands" to "NL", "holland" to "NL", "north macedonia" to "MK", "macedonia" to "MK",
        "norway" to "NO", "poland" to "PL", "portugal" to "PT", "romania" to "RO",
        "russia" to "RU", "san marino" to "SM", "serbia" to "RS", "slovakia" to "SK",
        "slovenia" to "SI", "spain" to "ES", "sweden" to "SE", "switzerland" to "CH",
        "ukraine" to "UA", "united kingdom" to "GB", "uk" to "GB", "england" to "GB",
        "scotland" to "GB", "wales" to "GB", "northern ireland" to "GB",
        "vatican city" to "VA", "vatican" to "VA",

        // Asia
        "afghanistan" to "AF", "armenia" to "AM", "azerbaijan" to "AZ", "bahrain" to "BH",
        "bangladesh" to "BD", "bhutan" to "BT", "brunei" to "BN", "cambodia" to "KH",
        "china" to "CN", "georgia" to "GE", "hong kong" to "HK", "india" to "IN",
        "indonesia" to "ID", "iran" to "IR", "iraq" to "IQ", "israel" to "IL",
        "japan" to "JP", "jordan" to "JO", "kazakhstan" to "KZ", "kuwait" to "KW",
        "kyrgyzstan" to "KG", "laos" to "LA", "lebanon" to "LB", "macau" to "MO",
        "malaysia" to "MY", "maldives" to "MV", "mongolia" to "MN", "myanmar" to "MM",
        "burma" to "MM", "nepal" to "NP", "north korea" to "KP", "oman" to "OM",
        "pakistan" to "PK", "palestine" to "PS", "philippines" to "PH", "qatar" to "QA",
        "saudi arabia" to "SA", "singapore" to "SG", "south korea" to "KR", "korea" to "KR",
        "sri lanka" to "LK", "syria" to "SY", "taiwan" to "TW", "tajikistan" to "TJ",
        "thailand" to "TH", "timor-leste" to "TL", "turkey" to "TR", "turkmenistan" to "TM",
        "united arab emirates" to "AE", "uae" to "AE", "uzbekistan" to "UZ",
        "vietnam" to "VN", "yemen" to "YE",

        // Africa
        "algeria" to "DZ", "angola" to "AO", "benin" to "BJ", "botswana" to "BW",
        "burkina faso" to "BF", "burundi" to "BI", "cameroon" to "CM", "cape verde" to "CV",
        "central african republic" to "CF", "chad" to "TD", "comoros" to "KM",
        "congo" to "CG", "democratic republic of the congo" to "CD", "djibouti" to "DJ",
        "egypt" to "EG", "equatorial guinea" to "GQ", "eritrea" to "ER", "ethiopia" to "ET",
        "gabon" to "GA", "gambia" to "GM", "ghana" to "GH", "guinea" to "GN",
        "guinea-bissau" to "GW", "ivory coast" to "CI", "cote d'ivoire" to "CI",
        "kenya" to "KE", "lesotho" to "LS", "liberia" to "LR", "libya" to "LY",
        "madagascar" to "MG", "malawi" to "MW", "mali" to "ML", "mauritania" to "MR",
        "mauritius" to "MU", "morocco" to "MA", "mozambique" to "MZ", "namibia" to "NA",
        "niger" to "NE", "nigeria" to "NG", "rwanda" to "RW", "sao tome and principe" to "ST",
        "senegal" to "SN", "seychelles" to "SC", "sierra leone" to "SL", "somalia" to "SO",
        "south africa" to "ZA", "south sudan" to "SS", "sudan" to "SD", "swaziland" to "SZ",
        "eswatini" to "SZ", "tanzania" to "TZ", "togo" to "TG", "tunisia" to "TN",
        "uganda" to "UG", "zambia" to "ZM", "zimbabwe" to "ZW",

        // Americas
        "antigua and barbuda" to "AG", "argentina" to "AR", "bahamas" to "BS",
        "barbados" to "BB", "belize" to "BZ", "bolivia" to "BO", "brazil" to "BR",
        "canada" to "CA", "chile" to "CL", "colombia" to "CO", "costa rica" to "CR",
        "cuba" to "CU", "dominica" to "DM", "dominican republic" to "DO", "ecuador" to "EC",
        "el salvador" to "SV", "grenada" to "GD", "guatemala" to "GT", "guyana" to "GY",
        "haiti" to "HT", "honduras" to "HN", "jamaica" to "JM", "mexico" to "MX",
        "nicaragua" to "NI", "panama" to "PA", "paraguay" to "PY", "peru" to "PE",
        "saint kitts and nevis" to "KN", "saint lucia" to "LC", "saint vincent and the grenadines" to "VC",
        "suriname" to "SR", "trinidad and tobago" to "TT", "united states" to "US",
        "united states of america" to "US", "usa" to "US", "us" to "US", "uruguay" to "UY",
        "venezuela" to "VE",

        // Oceania
        "australia" to "AU", "fiji" to "FJ", "kiribati" to "KI", "marshall islands" to "MH",
        "micronesia" to "FM", "nauru" to "NR", "new zealand" to "NZ", "palau" to "PW",
        "papua new guinea" to "PG", "samoa" to "WS", "solomon islands" to "SB",
        "tonga" to "TO", "tuvalu" to "TV", "vanuatu" to "VU"
    )

    fun getCountryEmoji(countryName: String?): String {
        if (countryName == null) return "🏳️"

        val isoCode = countryNameToISO[countryName.lowercase().trim()]
            ?: getISOCodeFromLocale(countryName)
            ?: return "🏳️"

        return convertISOToEmoji(isoCode)
    }

    private fun getISOCodeFromLocale(countryName: String): String? {
        return try {
            Locale.getISOCountries().find { code ->
                Locale("", code).displayCountry.equals(countryName, ignoreCase = true)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun convertISOToEmoji(isoCode: String): String {
        if (isoCode.length != 2) return "🏳️"

        val firstChar = Character.codePointAt(isoCode.uppercase(), 0) - 0x41 + 0x1F1E6
        val secondChar = Character.codePointAt(isoCode.uppercase(), 1) - 0x41 + 0x1F1E6

        return String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
    }
}