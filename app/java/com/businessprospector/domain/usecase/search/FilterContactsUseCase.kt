


class FilterContactsUseCase @Inject constructor() {
    operator fun invoke(
        contacts: List<Contact>,
        filters: Map<String, String>
    ): List<Contact> {
        var filteredContacts = contacts

        // Filtrowanie według statusu
        filters["status"]?.let { status ->
            filteredContacts = filteredContacts.filter { it.status == status }
        }

        // Filtrowanie według kategorii
        filters["category"]?.let { category ->
            filteredContacts = filteredContacts.filter { it.category == category }
        }

        // Filtrowanie według sourcee
        filters["source"]?.let { source ->
            filteredContacts = filteredContacts.filter { it.source == source }
        }

        // Filtrowanie według tagów
        filters["tag"]?.let { tag ->
            filteredContacts = filteredContacts.filter { it.tags.contains(tag) }
        }

        // Filtrowanie po dacie utworzenia - od
        filters["created_from"]?.let { fromDate ->
            val date = java.text.SimpleDateFormat("yyyy-MM-dd").parse(fromDate)
            filteredContacts = filteredContacts.filter { it.createdAt?.after(date) ?: false }
        }

        // Filtrowanie po dacie utworzenia - do
        filters["created_to"]?.let { toDate ->
            val date = java.text.SimpleDateFormat("yyyy-MM-dd").parse(toDate)
            filteredContacts = filteredContacts.filter { it.createdAt?.before(date) ?: false }
        }

        return filteredContacts
    }
}
