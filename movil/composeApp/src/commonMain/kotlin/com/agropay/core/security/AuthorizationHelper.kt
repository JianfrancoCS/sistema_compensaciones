package com.agropay.core.security

import com.agropay.domain.model.UserInfo

/**
 * Helper simple para autorización similar a Spring Security
 *
 * Uso:
 * - HAS_GROUP(userInfo, "SUPERVISOR")
 * - HAS_ROLE(userInfo, "CREAR_TAREOS")
 * - HAS_AUTHORIZE(userInfo, "SUPERVISOR") // busca en grupos o roles
 */

/**
 * Verifica si el usuario tiene un GRUPO específico
 */
fun HAS_GROUP(userInfo: UserInfo?, group: String): Boolean {
    if (userInfo == null) {
        println("🔒 HAS_GROUP('$group') = FALSE (userInfo es null)")
        return false
    }
    val hasGroup = userInfo.groups.any { it.equals(group, ignoreCase = true) }
    println("🔒 HAS_GROUP('$group') = $hasGroup (grupos del usuario: ${userInfo.groups})")
    return hasGroup
}

/**
 * Verifica si el usuario tiene un ROL específico
 * (Por ahora siempre devuelve false hasta que agregues roles al UserInfo)
 */
fun HAS_ROLE(userInfo: UserInfo?, role: String): Boolean {
    // TODO: Agregar soporte de roles cuando lo necesites
    println("🔒 HAS_ROLE('$role') = FALSE (no implementado)")
    return false
}

/**
 * Verifica si el usuario tiene autorización (busca en GRUPOS o ROLES)
 */
fun HAS_AUTHORIZE(userInfo: UserInfo?, authority: String): Boolean {
    val result = HAS_GROUP(userInfo, authority) || HAS_ROLE(userInfo, authority)
    println("🔐 HAS_AUTHORIZE('$authority') = $result")
    return result
}