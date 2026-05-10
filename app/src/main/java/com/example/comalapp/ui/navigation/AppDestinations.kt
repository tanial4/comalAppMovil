package com.example.comalapp.ui.navigation

object AppDestinations {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val STUDENT_GRAPH = "student"
    const val STUDENT_HOME = "student/home"
    const val STUDENT_MENU = "student/menu"
    const val STUDENT_CART = "student/cart"
    const val STUDENT_ORDER_CONFIRM = "student/order/confirm"
    const val STUDENT_ORDER_HISTORY = "student/order/history"
    const val STUDENT_ORDER_DETAIL = "student/order/detail/{orderId}"
    const val STUDENT_PROFILE = "student/profile"
    const val STUDENT_NOTIFICATIONS  = "student/notifications"
    const val STUDENT_ORDER_STATUS = "student/order/status/{orderId}"
    const val STUDENT_TICKET = "student/ticket/{orderId}"



    const val WORKER_ORDERS = "worker/orders"
    const val WORKER_ORDER_DETAIL = "worker/order/detail/{orderId}"
    const val WORKER_QR_SCANNER = "worker/qr"
    const val WORKER_GRAPH = "worker_graph"
    const val WORKER_HOME = "worker/home"
    const val WORKER_PRODUCTS = "worker/products"

    const val ADMIN_GRAPH = "admin"
    const val ADMIN_PRODUCTS = "admin/products"
    const val ADMIN_PRODUCT_FORM = "admin/products/form?productId={productId}"
    const val ADMIN_ORDERS = "admin/orders"
    const val ADMIN_USERS = "admin/users"
    const val ADMIN_DASHBOARD = "admin/dashboard"
    const val ADMIN_WORKERS = "admin/workers"
    const val ADMIN_WORKER_FORM = "admin/workers/form"
    const val ADMIN_ORDER_DETAIL = "admin/orders/detail/{orderId}"
    const val ADMIN_USER_DETAIL = "admin/users/detail/{userId}"

    fun studentOrderDetail(orderId: String) = "student/order/detail/$orderId"
    fun studentOrderStatus(orderId: String) = "student/order/status/$orderId"
    fun studentTicket(orderId: String) = "student/ticket/$orderId"
    fun workerOrderDetail(orderId: String) = "worker/order/detail/$orderId"
    fun adminProductForm(productId: String? = null) =
        if (productId != null) "admin/products/form?productId=$productId"
        else "admin/products/form"
    fun adminOrderDetail(orderId: String) = "admin/orders/detail/$orderId"
    fun adminUserDetail(userId: String) = "admin/users/detail/$userId"
}