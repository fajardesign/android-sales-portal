package com.example.and_sales_portal.data.partner

/** Status transitions of the Partner Pipeline (PRD v3 A2), the same effects as the web portal's `advance`. */
object PartnerActions {

    fun move(p: Partner, to: PartnerStatus, at: Long, by: String, reason: String?): Partner {
        var stores = p.stores
        if (to == PartnerStatus.ACTIVE) {
            val code = p.id.takeLast(4)
            stores = stores.mapIndexed { i, s -> s.copy(status = "ACTIVE", code = s.code ?: "TK$code-${(i + 1).toString().padStart(2, '0')}") }
        }
        if (to == PartnerStatus.INACTIVE) stores = stores.map { it.copy(status = "INACTIVE", assigned = emptyList()) }
        return p.copy(
            status = to,
            statusUpdatedAt = at,
            stores = stores,
            history = p.history + StatusEvent(at, p.status, to, by, reason),
            statusReason = if (to.isFinal) reason else p.statusReason,
            revision = if (to == PartnerStatus.REVISION_REQUIRED) p.revision else if (to == PartnerStatus.UNDER_REVIEW) null else p.revision,
        )
    }
}
