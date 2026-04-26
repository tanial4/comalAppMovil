package com.example.comalapp.ui.navigation

object AppDestinations {
    const val LOGIN = "login"
    const val REGISTER = "register"

    const val STUDENT_HOME = "student/home"
    const val STUDENT_MENU = "student/menu"
    const val STUDENT_CART = "student/cart"
    const val STUDENT_ORDER_CONFIRM = "student/order/confirm"
    const val STUDENT_ORDER_STATUS = "student/order/status"
    const val STUDENT_ORDER_HISTORY = "student/order/history"
    const val STUDENT_ORDER_DETAIL = "student/order/detail/{orderId}"
    const val STUDENT_PROFILE = "student/profile"

    const val WORKER_ORDERS = "worker/orders"
    const val WORKER_ORDER_DETAIL = "worker/order/detail/{orderId}"
    const val WORKER_QR_SCANNER = "worker/qr"

    const val ADMIN_PRODUCTS = "admin/products"
    const val ADMIN_PRODUCT_FORM = "admin/products/form?productId={productId}"
    const val ADMIN_ORDERS = "admin/orders"
    const val ADMIN_USERS = "admin/users"

    fun studentOrderDetail(orderId: String) = "student/order/detail/$orderId"
    fun workerOrderDetail(orderId: String) = "worker/order/detail/$orderId"
    fun adminProductForm(productId: String? = null) =
        if (productId != null) "admin/products/form?productId=$productId"
        else "admin/products/form"
}