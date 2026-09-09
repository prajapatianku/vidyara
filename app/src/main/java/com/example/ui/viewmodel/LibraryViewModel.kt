package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.LibraryRepository
import com.example.data.repository.PlatformRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class DashboardMetrics(
    val activeStudentsCount: Int,
    val presentTodayCount: Int,
    val absentTodayCount: Int,
    val occupiedSeatsCount: Int,
    val totalSeatsCount: Int,
    val availableSeatsCount: Int,
    val todayCollection: Int,
    val monthlyCollection: Int,
    val totalExpenses: Int,
    val netRevenue: Int,
    val pendingDues: Int,
    val expiringCount: Int,
    val pendingRequestsCount: Int
)

class LibraryViewModel(
    private val repository: LibraryRepository = LibraryRepository()
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Base Repository State
    val ownerProfile = repository.ownerProfile
    val branches = repository.branches
    val activeBranchId = repository.activeBranchId
    val saasSubscription = repository.saasSubscription
    val saasPurchaseHistory = repository.saasPurchaseHistory
    val registrationRequests = repository.registrationRequests
    val auditLogs = repository.auditLogs
    val isLoggedIn = repository.isLoggedIn
    val isOnboardingCompleted = repository.isOnboardingCompleted
    val syncInfo = repository.syncInfo

    // Platform (Super Admin) Repository & State
    val platformRepository: PlatformRepository = PlatformRepository.getInstance()
    val platformPricing = platformRepository.pricing
    val platformCoupons = platformRepository.coupons
    val platformTransactions = platformRepository.transactions
    val platformBroadcasts = platformRepository.broadcasts
    val platformAppControl = platformRepository.appControl
    val isSuperAdminAuthenticated = platformRepository.isSuperAdminAuthenticated
    val superAdminOtp = platformRepository.superAdminOtp

    init {
        repository.checkSubscriptionStatus()
    }

    // Separated & Filtered Branch-Specific States
    val library = combine(repository.library, repository.activeBranchId, repository.branches) { mainLib, activeId, branchList ->
        val activeBranch = branchList.find { it.id == activeId }
        if (activeBranch != null) {
            Library(
                id = activeBranch.id,
                ownerId = mainLib.ownerId,
                name = activeBranch.name,
                phone = activeBranch.phone,
                email = mainLib.email,
                address = activeBranch.address,
                location = activeBranch.address,
                city = activeBranch.city,
                state = activeBranch.state,
                pincode = activeBranch.pincode,
                totalSeats = activeBranch.totalSeats,
                openingTime = activeBranch.openingTime,
                closingTime = activeBranch.closingTime,
                upiId = activeBranch.upiId,
                registrationToken = mainLib.registrationToken
            )
        } else {
            mainLib
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.library.value)

    val shifts = combine(repository.shifts, activeBranchId) { list, activeId ->
        list.filter { it.branchId == activeId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val libraryPlans = combine(repository.libraryPlans, activeBranchId) { list, activeId ->
        list.filter { it.branchId == activeId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val seats = combine(repository.seats, activeBranchId) { list, activeId ->
        list.filter { it.branchId == activeId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val students = combine(repository.students, activeBranchId) { list, activeId ->
        list.filter { it.branchId == activeId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attendance = combine(repository.attendance, activeBranchId) { list, activeId ->
        list.filter { it.branchId == activeId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments = combine(repository.payments, activeBranchId) { list, activeId ->
        list.filter { it.branchId == activeId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses = combine(repository.expenses, activeBranchId) { list, activeId ->
        list.filter { it.branchId == activeId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val lastGeneratedOtp = repository.lastGeneratedOtp

    private val _isHindi = MutableStateFlow(false)
    val isHindi = _isHindi.asStateFlow()

    fun setHindiLanguage(enabled: Boolean) {
        _isHindi.value = enabled
    }

    // UI Search & Filter States
    private val _studentSearchQuery = MutableStateFlow("")
    val studentSearchQuery = _studentSearchQuery.asStateFlow()

    private val _studentShiftFilter = MutableStateFlow("All")
    val studentShiftFilter = _studentShiftFilter.asStateFlow()

    private val _studentStatusFilter = MutableStateFlow("All")
    val studentStatusFilter = _studentStatusFilter.asStateFlow()

    private val _seatFloorFilter = MutableStateFlow("All")
    val seatFloorFilter = _seatFloorFilter.asStateFlow()

    private val _attendanceShiftFilter = MutableStateFlow("All")
    val attendanceShiftFilter = _attendanceShiftFilter.asStateFlow()

    private val _selectedAttendanceDate = MutableStateFlow(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    val selectedAttendanceDate = _selectedAttendanceDate.asStateFlow()

    private val _reportType = MutableStateFlow("Revenue")
    val reportType = _reportType.asStateFlow()

    private val _financeSubTab = MutableStateFlow(0) // 0 = Fees, 1 = Expenses
    val financeSubTab = _financeSubTab.asStateFlow()

    // Dialog & Modal Triggers
    private val _selectedStudentForDetail = MutableStateFlow<Student?>(null)
    val selectedStudentForDetail = _selectedStudentForDetail.asStateFlow()

    private val _selectedSeatForAction = MutableStateFlow<Seat?>(null)
    val selectedSeatForAction = _selectedSeatForAction.asStateFlow()

    private val _activeReceipt = MutableStateFlow<StudentPayment?>(null)
    val activeReceipt = _activeReceipt.asStateFlow()

    private val _showUpgradeModal = MutableStateFlow(false)
    val showUpgradeModal = _showUpgradeModal.asStateFlow()

    private val _upgradeTargetFeature = MutableStateFlow<String?>(null)
    val upgradeTargetFeature = _upgradeTargetFeature.asStateFlow()

    private val _showAddStudentDialog = MutableStateFlow(false)
    val showAddStudentDialog = _showAddStudentDialog.asStateFlow()

    private val _showCollectFeeDialog = MutableStateFlow<Student?>(null)
    val showCollectFeeDialog = _showCollectFeeDialog.asStateFlow()

    private val _showAddExpenseDialog = MutableStateFlow(false)
    val showAddExpenseDialog = _showAddExpenseDialog.asStateFlow()

    private val _showQrDialog = MutableStateFlow(false)
    val showQrDialog = _showQrDialog.asStateFlow()

    private val _showBranchManagerDialog = MutableStateFlow(false)
    val showBranchManagerDialog = _showBranchManagerDialog.asStateFlow()

    private val _showWhatsAppReminderDialog = MutableStateFlow(false)
    val showWhatsAppReminderDialog = _showWhatsAppReminderDialog.asStateFlow()

    private val _selectedStudentForWhatsAppReminder = MutableStateFlow<Student?>(null)
    val selectedStudentForWhatsAppReminder = _selectedStudentForWhatsAppReminder.asStateFlow()

    private val _uiToastMessage = MutableStateFlow<String?>(null)
    val uiToastMessage = _uiToastMessage.asStateFlow()

    // Computed Dashboard KPI Metrics
    val dashboardMetrics: StateFlow<DashboardMetrics> = combine(
        combine(students, seats) { stu, seat -> Pair(stu, seat) },
        combine(attendance, payments) { att, pay -> Pair(att, pay) },
        combine(expenses, registrationRequests) { exp, req -> Pair(exp, req) }
    ) { (stuList, seatList), (attList, payList), (expList, reqList) ->
        val today = dateFormat.format(Date())
        val activeStu = stuList.count { it.status == StudentStatus.ACTIVE }
        val presentToday = attList.count { it.attendanceDate == today && it.status == AttendanceStatus.PRESENT }
        val absentToday = attList.count { it.attendanceDate == today && it.status == AttendanceStatus.ABSENT }
        val occupied = seatList.count { it.status == SeatStatus.OCCUPIED }
        val totalSeats = seatList.size
        val availableSeats = totalSeats - occupied

        val todayCol = payList.filter { it.paymentDate == today }.sumOf { it.amount }
        val monthCol = payList.sumOf { it.amount }
        val totalExp = expList.sumOf { it.amount }
        val netRev = monthCol - totalExp
        val pendingDues = stuList.sumOf { it.dueAmount }
        val expiring = stuList.count { it.dueAmount > 0 || it.status == StudentStatus.EXPIRED }
        val pendingReqs = reqList.count { it.status == "pending" }

        DashboardMetrics(
            activeStudentsCount = activeStu,
            presentTodayCount = presentToday,
            absentTodayCount = absentToday,
            occupiedSeatsCount = occupied,
            totalSeatsCount = totalSeats,
            availableSeatsCount = availableSeats,
            todayCollection = todayCol,
            monthlyCollection = monthCol,
            totalExpenses = totalExp,
            netRevenue = netRev,
            pendingDues = pendingDues,
            expiringCount = expiring,
            pendingRequestsCount = pendingReqs
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DashboardMetrics(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
    )

    fun getStudentDaysUntilDue(student: Student): Int {
        if (student.feeDueDate.isBlank()) return 999
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dueDate = sdf.parse(student.feeDueDate) ?: return 999
            val todayCal = Calendar.getInstance()
            todayCal.set(Calendar.HOUR_OF_DAY, 0)
            todayCal.set(Calendar.MINUTE, 0)
            todayCal.set(Calendar.SECOND, 0)
            todayCal.set(Calendar.MILLISECOND, 0)
            val diffMs = dueDate.time - todayCal.timeInMillis
            (diffMs / (1000 * 60 * 60 * 24)).toInt()
        } catch (e: Exception) {
            999
        }
    }

    fun isStudentInGracePeriod(student: Student): Boolean {
        val days = getStudentDaysUntilDue(student)
        return days in -2..-1 && student.dueAmount > 0
    }

    fun isStudentExpiringSoon(student: Student): Boolean {
        val days = getStudentDaysUntilDue(student)
        return days in 0..3 && student.dueAmount > 0
    }

    // Filtered Students List
    val filteredStudents: StateFlow<List<Student>> = combine(
        students, studentSearchQuery, studentShiftFilter, studentStatusFilter
    ) { list, query, shift, status ->
        list.filter { student ->
            val matchesQuery = query.isBlank() ||
                student.fullName.contains(query, ignoreCase = true) ||
                student.studentCode.contains(query, ignoreCase = true) ||
                student.mobile.contains(query) ||
                student.assignedSeatNumber.contains(query, ignoreCase = true)

            val matchesShift = shift == "All" || student.assignedShiftName.contains(shift, ignoreCase = true)
            val matchesStatus = when (status) {
                "Active" -> student.status == StudentStatus.ACTIVE
                "Has Due" -> student.dueAmount > 0
                "Expired" -> student.status == StudentStatus.EXPIRED || getStudentDaysUntilDue(student) < -2
                "Expiring in 3 Days" -> {
                    val days = getStudentDaysUntilDue(student)
                    days in -2..3 && (student.dueAmount > 0 || student.status == StudentStatus.EXPIRED)
                }
                else -> true
            }
            matchesQuery && matchesShift && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Setters & Actions
    fun setStudentSearchQuery(q: String) { _studentSearchQuery.value = q }
    fun setStudentShiftFilter(f: String) { _studentShiftFilter.value = f }
    fun setStudentStatusFilter(s: String) { _studentStatusFilter.value = s }
    fun setSeatFloorFilter(floor: String) { _seatFloorFilter.value = floor }
    fun setAttendanceShiftFilter(shift: String) { _attendanceShiftFilter.value = shift }
    fun setSelectedAttendanceDate(date: String) { _selectedAttendanceDate.value = date }

    fun goToPreviousAttendanceDay() {
        try {
            val cal = Calendar.getInstance()
            cal.time = dateFormat.parse(_selectedAttendanceDate.value) ?: Date()
            cal.add(Calendar.DAY_OF_YEAR, -1)
            _selectedAttendanceDate.value = dateFormat.format(cal.time)
        } catch (e: Exception) {
            // keep current
        }
    }

    fun goToNextAttendanceDay() {
        try {
            val cal = Calendar.getInstance()
            cal.time = dateFormat.parse(_selectedAttendanceDate.value) ?: Date()
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val nextDate = cal.time
            // Do not advance past today if desired, or allow viewing up to today
            _selectedAttendanceDate.value = dateFormat.format(nextDate)
        } catch (e: Exception) {
            // keep current
        }
    }

    fun isAttendanceDateEditable(dateStr: String): Boolean {
        return try {
            val target = dateFormat.parse(dateStr) ?: return false
            val today = dateFormat.parse(dateFormat.format(Date())) ?: return false
            val diffMillis = today.time - target.time
            val diffDays = (diffMillis / (1000 * 60 * 60 * 24)).toInt()
            // Editable for Today (0), Yesterday (1), Day before yesterday (2)
            diffDays in 0..2
        } catch (e: Exception) {
            false
        }
    }

    fun getAttendanceDaysAgo(dateStr: String): Int {
        return try {
            val target = dateFormat.parse(dateStr) ?: return 0
            val today = dateFormat.parse(dateFormat.format(Date())) ?: return 0
            val diffMillis = today.time - target.time
            (diffMillis / (1000 * 60 * 60 * 24)).toInt()
        } catch (e: Exception) {
            0
        }
    }

    fun setReportType(type: String) { _reportType.value = type }
    fun setFinanceSubTab(tab: Int) { _financeSubTab.value = tab }

    fun selectStudentForDetail(student: Student?) { _selectedStudentForDetail.value = student }
    fun selectSeatForAction(seat: Seat?) { _selectedSeatForAction.value = seat }
    fun showAddStudentDialog(show: Boolean) { _showAddStudentDialog.value = show }
    fun showCollectFeeDialog(student: Student?) { _showCollectFeeDialog.value = student }
    fun showAddExpenseDialog(show: Boolean) { _showAddExpenseDialog.value = show }
    fun showQrDialog(show: Boolean) { _showQrDialog.value = show }
    fun showBranchManagerDialog(show: Boolean) {
        if (!hasFeature("multi_branch")) {
            requestUpgrade("multi_branch")
        } else {
            _showBranchManagerDialog.value = show
        }
    }

    fun showWhatsAppReminderDialog(show: Boolean) {
        if (!hasFeature("whatsapp_fee_reminders")) {
            requestUpgrade("whatsapp_fee_reminders")
        } else {
            _showWhatsAppReminderDialog.value = show
        }
    }

    fun selectStudentForWhatsAppReminder(student: Student?) {
        _selectedStudentForWhatsAppReminder.value = student
        if (student != null) {
            _showWhatsAppReminderDialog.value = true
        }
    }

    fun showActiveReceipt(payment: StudentPayment?) { _activeReceipt.value = payment }
    fun clearToast() { _uiToastMessage.value = null }
    fun showToast(message: String) { _uiToastMessage.value = message }

    fun requestUpgrade(featureKey: String) {
        _upgradeTargetFeature.value = featureKey
        _showUpgradeModal.value = true
    }

    fun dismissUpgradeModal() {
        _showUpgradeModal.value = false
        _upgradeTargetFeature.value = null
    }

    fun hasFeature(featureKey: String): Boolean = repository.hasFeature(featureKey)
    fun requiredPlan(featureKey: String): SaaSPlanType = repository.requiredPlan(featureKey)
    fun canAddStudent(): Boolean = repository.canAddStudent()
    fun getMaxStudentsAllowed(): Int = repository.getMaxStudentsAllowed()

    fun completeOnboarding(
        name: String,
        phone: String,
        address: String,
        city: String,
        state: String,
        pincode: String,
        openingTime: String,
        closingTime: String
    ) {
        repository.completeOnboarding(name, phone, address, city, state, pincode, openingTime, closingTime)
        _uiToastMessage.value = "Welcome to Vidyara! Your workspace is ready."
    }

    fun switchBranch(branchId: String) {
        repository.switchBranch(branchId)
        _uiToastMessage.value = "Switched branch"
    }

    fun createBranch(
        name: String,
        code: String,
        address: String,
        phone: String,
        city: String,
        state: String,
        pincode: String,
        totalSeats: Int,
        openingTime: String,
        closingTime: String,
        upiId: String
    ) {
        val sub = saasSubscription.value
        val currentBranchCount = branches.value.size
        if (currentBranchCount >= sub.allowedBranchesCount) {
            requestUpgrade("add_branch")
            return
        }
        val success = repository.createBranch(
            name = name,
            code = code,
            address = address,
            phone = phone,
            city = city,
            state = state,
            pincode = pincode,
            totalSeats = totalSeats,
            openingTime = openingTime,
            closingTime = closingTime,
            upiId = upiId
        )
        if (success) {
            _uiToastMessage.value = "Branch $name created successfully!"
            _showBranchManagerDialog.value = false
        }
    }

    fun upgradeSaaS(
        planType: SaaSPlanType, 
        period: BillingPeriod, 
        allowedBranches: Int = 1,
        razorpayPaymentId: String? = null,
        paidAmount: Int? = null,
        discountAmount: Int = 0,
        couponCode: String? = null
    ) {
        repository.upgradeSaaSPlan(planType, period, allowedBranches, razorpayPaymentId)
        if (!razorpayPaymentId.isNullOrBlank()) {
            val amount = paidAmount ?: when (planType) {
                SaaSPlanType.PREMIUM -> if (period == BillingPeriod.MONTHLY) platformPricing.value.proMonthlyPrice else platformPricing.value.proYearlyPrice
                SaaSPlanType.BUSINESS -> if (period == BillingPeriod.MONTHLY) platformPricing.value.businessMonthlyPrice else platformPricing.value.businessYearlyPrice
                else -> 0
            }
            recordPlatformPurchase(
                razorpayPaymentId = razorpayPaymentId,
                planName = planType.displayName,
                billingPeriod = if (period == BillingPeriod.MONTHLY) "Monthly" else "Yearly",
                amount = amount,
                discountAmount = discountAmount,
                couponCode = couponCode
            )
        }
        _showUpgradeModal.value = false
        _upgradeTargetFeature.value = null
        _uiToastMessage.value = "Upgraded to ${planType.displayName}! All features unlocked."
    }

    fun purchaseAdditionalBranch(razorpayPaymentId: String? = null, paidAmount: Int? = null) {
        val proratedPrice = calculateProratedBranchPrice()
        repository.addSaaSSubscriptionBranch(razorpayPaymentId, proratedPrice)
        if (!razorpayPaymentId.isNullOrBlank()) {
            recordPlatformPurchase(
                razorpayPaymentId = razorpayPaymentId,
                planName = "Additional Branch Add-On",
                billingPeriod = "Prorated Cycle",
                amount = paidAmount ?: proratedPrice,
                discountAmount = 0,
                couponCode = null
            )
        }
        _showUpgradeModal.value = false
        _uiToastMessage.value = "Additional branch added to your subscription successfully!"
    }

    fun renewSaaS(
        razorpayPaymentId: String? = null,
        paidAmount: Int? = null,
        discountAmount: Int = 0,
        couponCode: String? = null
    ) {
        repository.renewSaaSPlan(razorpayPaymentId)
        if (!razorpayPaymentId.isNullOrBlank()) {
            val sub = saasSubscription.value
            val amount = paidAmount ?: when (sub.planType) {
                SaaSPlanType.PREMIUM -> if (sub.billingPeriod == BillingPeriod.MONTHLY) platformPricing.value.proMonthlyPrice else platformPricing.value.proYearlyPrice
                SaaSPlanType.BUSINESS -> if (sub.billingPeriod == BillingPeriod.MONTHLY) platformPricing.value.businessMonthlyPrice else platformPricing.value.businessYearlyPrice
                else -> 0
            }
            recordPlatformPurchase(
                razorpayPaymentId = razorpayPaymentId,
                planName = sub.planType.displayName,
                billingPeriod = if (sub.billingPeriod == BillingPeriod.MONTHLY) "Monthly Renewal" else "Yearly Renewal",
                amount = amount,
                discountAmount = discountAmount,
                couponCode = couponCode
            )
        }
        _showUpgradeModal.value = false
        _uiToastMessage.value = "Subscription renewed successfully!"
    }

    fun requestSuperAdminOtp(emailOrPhone: String): String {
        return platformRepository.requestSuperAdminOtp(emailOrPhone)
    }

    fun verifySuperAdminLogin(emailOrPhone: String, pin: String, otp: String): Boolean {
        return platformRepository.verifySuperAdminLogin(emailOrPhone, pin, otp)
    }

    fun logoutSuperAdmin() {
        platformRepository.logoutSuperAdmin()
    }

    fun validateCoupon(code: String, planName: String, basePrice: Int): Pair<Boolean, Int> {
        return platformRepository.validateCoupon(code, planName, basePrice)
    }

    fun recordPlatformPurchase(
        razorpayPaymentId: String,
        planName: String,
        billingPeriod: String,
        amount: Int,
        discountAmount: Int = 0,
        couponCode: String? = null
    ) {
        val owner = ownerProfile.value
        val lib = repository.library.value
        val tx = PlatformTransaction(
            transactionId = razorpayPaymentId,
            accountId = owner.userId.ifBlank { owner.id },
            ownerName = owner.fullName,
            ownerPhone = owner.phone,
            libraryName = lib.name,
            planName = planName,
            billingPeriod = billingPeriod,
            amount = amount,
            discountAmount = discountAmount,
            couponCode = couponCode,
            timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
            status = "SUCCESS"
        )
        platformRepository.recordTransaction(tx)
        if (!couponCode.isNullOrBlank()) {
            platformRepository.recordCouponUsed(couponCode)
        }
    }

    fun calculateProratedBranchPrice(): Int {
        val sub = saasSubscription.value
        if (sub.planType != SaaSPlanType.BUSINESS) return 0
        
        val daysRemaining = getSubscriptionDaysRemaining()
        if (daysRemaining <= 0) return 0
        
        val totalDays = if (sub.billingPeriod == BillingPeriod.MONTHLY) 28 else 168
        val baseAdditionalPrice = if (sub.billingPeriod == BillingPeriod.MONTHLY) {
            platformPricing.value.additionalBranchMonthlyPrice
        } else {
            platformPricing.value.additionalBranchYearlyPrice
        }
        
        val proratedPrice = (baseAdditionalPrice * daysRemaining) / totalDays
        return proratedPrice.coerceAtLeast(1)
    }

    fun generateSaaSInvoiceText(record: SaaSPurchaseRecord): String {
        val owner = ownerProfile.value
        val lib = repository.library.value

        val sb = StringBuilder()
        sb.append("========================================\n")
        sb.append("🏛️ VIDYARA - OFFICIAL TAX INVOICE\n")
        sb.append("Vidyara Technologies Pvt. Ltd.\n")
        sb.append("========================================\n\n")

        sb.append("📄 INVOICE DETAILS:\n")
        sb.append("• Invoice No: ${record.invoiceNumber}\n")
        sb.append("• Date & Time: ${record.timestamp}\n")
        sb.append("• Payment Status: ${record.status} (Paid)\n")
        sb.append("• Razorpay Ref No: ${record.razorpayPaymentId}\n\n")

        sb.append("👤 BILLED TO:\n")
        sb.append("• Library Name: ${lib.name}\n")
        sb.append("• Owner Name: ${owner.fullName}\n")
        sb.append("• Phone: ${owner.phone}\n")
        sb.append("• Email: ${owner.email}\n")
        sb.append("• Address: ${lib.address}, ${lib.city}, ${lib.state} - ${lib.pincode}\n\n")

        sb.append("----------------------------------------\n")
        sb.append("📦 ITEM DESCRIPTION:\n")
        sb.append("----------------------------------------\n")
        sb.append("1. ${record.productName}\n")
        sb.append("   - Duration / Period: ${record.billingPeriod}\n")
        sb.append("   - Active Branches: ${record.branchCount}\n")
        sb.append("   - Access: Unlimited Seats, WhatsApp Alerts & Cloud Backup\n\n")

        val subtotal = (record.amount * 100) / 118
        val gst = record.amount - subtotal

        sb.append("----------------------------------------\n")
        sb.append("💰 PAYMENT BREAKUP (INR):\n")
        sb.append("• Base Amount: ₹$subtotal\n")
        sb.append("• GST (18% inclusive): ₹$gst\n")
        sb.append("• Total Amount Paid: ₹${record.amount}\n")
        sb.append("----------------------------------------\n\n")

        sb.append("✅ Payment Mode: Razorpay Secured Online Payment\n")
        sb.append("💡 Support: support@vidyara.com | www.vidyara.com\n")
        sb.append("========================================\n")
        sb.append("Thank you for choosing Vidyara to power your library!\n")
        return sb.toString()
    }

    fun downloadOrShareSaaSInvoice(context: android.content.Context, record: SaaSPurchaseRecord) {
        val invoiceText = generateSaaSInvoiceText(record)
        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Vidyara Invoice - ${record.invoiceNumber}")
            putExtra(android.content.Intent.EXTRA_TEXT, invoiceText)
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(android.content.Intent.createChooser(shareIntent, "Download / Share Invoice (${record.invoiceNumber})"))
            _uiToastMessage.value = "Invoice ${record.invoiceNumber} ready to download / print!"
        } catch (e: Exception) {
            _uiToastMessage.value = "Unable to open sharing application"
        }
    }

    fun getSubscriptionDaysRemaining(): Int {
        val sub = saasSubscription.value
        if (sub.planType == SaaSPlanType.FREE) return -1
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val endDate = sdf.parse(sub.endDate) ?: return -1
            val today = sdf.parse(sdf.format(Date())) ?: return -1
            val diff = endDate.time - today.time
            (diff / (1000 * 60 * 60 * 24)).toInt()
        } catch (e: Exception) {
            -1
        }
    }

    fun updateLibraryDetails(
        name: String,
        phone: String,
        address: String,
        city: String,
        state: String,
        pincode: String,
        totalSeats: Int,
        openingTime: String,
        closingTime: String,
        upiId: String
    ) {
        repository.updateLibraryDetails(
            libraryName = name,
            phone = phone,
            address = address,
            city = city,
            state = state,
            pincode = pincode,
            totalSeats = totalSeats,
            openingTime = openingTime,
            closingTime = closingTime,
            upiId = upiId
        )
        _uiToastMessage.value = "Library profile updated successfully!"
    }

    fun calculateNextDueDate(admissionDateStr: String, cycleDays: Int = 28): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val admissionDate = sdf.parse(admissionDateStr) ?: return ""
            val today = sdf.parse(sdf.format(Date())) ?: Date()

            val cal = Calendar.getInstance()
            cal.time = admissionDate

            if (cal.time.after(today) || cal.time == today) {
                cal.add(Calendar.DAY_OF_YEAR, cycleDays)
            } else {
                while (!cal.time.after(today)) {
                    cal.add(Calendar.DAY_OF_YEAR, cycleDays)
                }
            }
            sdf.format(cal.time)
        } catch (e: Exception) {
            ""
        }
    }

    fun createStudentAndReturn(
        fullName: String,
        mobile: String,
        whatsapp: String,
        email: String,
        course: String,
        assignedSeat: String,
        assignedShift: String,
        monthlyFee: Int,
        initialDue: Int,
        gender: String = "Male",
        address: String = "",
        joiningDate: String = "",
        feeDueDate: String = ""
    ): Student? {
        if (!canAddStudent()) {
            requestUpgrade("student_limit_20")
            _uiToastMessage.value = "Free plan is limited to 20 students. Upgrade to Vidyara Pro to add unlimited students!"
            return null
        }
        val count = students.value.size + 1
        val code = "STU-" + String.format(Locale.US, "%03d", count)
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val actualJoiningDate = joiningDate.ifBlank { todayStr }
        val actualDueDate = feeDueDate.ifBlank { calculateNextDueDate(actualJoiningDate) }

        val student = Student(
            studentCode = code,
            fullName = fullName,
            mobile = mobile,
            whatsapp = if (whatsapp.isBlank()) mobile else whatsapp,
            email = email,
            course = course,
            assignedSeatNumber = assignedSeat,
            assignedShiftName = assignedShift,
            monthlyFee = monthlyFee,
            dueAmount = initialDue,
            branchId = repository.activeBranchId.value,
            gender = gender,
            address = address,
            joiningDate = actualJoiningDate,
            feeDueDate = actualDueDate
        )
        val success = repository.addStudent(student)
        return if (success) {
            _showAddStudentDialog.value = false
            _uiToastMessage.value = "Student $fullName ($code) added successfully!"
            student
        } else {
            _uiToastMessage.value = "Seat $assignedSeat is already occupied in $assignedShift. Please choose another seat."
            null
        }
    }

    fun createStudent(
        fullName: String,
        mobile: String,
        whatsapp: String,
        email: String,
        course: String,
        assignedSeat: String,
        assignedShift: String,
        monthlyFee: Int,
        initialDue: Int,
        gender: String = "Male",
        address: String = "",
        joiningDate: String = "",
        feeDueDate: String = ""
    ): Boolean {
        return createStudentAndReturn(
            fullName, mobile, whatsapp, email, course, assignedSeat, assignedShift, monthlyFee, initialDue, gender, address, joiningDate, feeDueDate
        ) != null
    }

    fun deleteStudent(studentId: String) {
        repository.deleteStudent(studentId)
        _selectedStudentForDetail.value = null
        _uiToastMessage.value = "Student removed"
    }

    fun releaseStudentSeat(studentId: String) {
        val student = students.value.find { it.id == studentId }
        val seatNum = student?.assignedSeatNumber ?: ""
        val releasedSeat = repository.releaseStudentSeat(studentId)
        _uiToastMessage.value = if (releasedSeat.isNotBlank()) {
            "Seat $releasedSeat released and is now available for walk-ins!"
        } else {
            "Seat unassigned successfully!"
        }
    }

    fun assignSeatToStudent(seatNumber: String, studentId: String, studentName: String, shiftName: String) {
        repository.assignSeat(seatNumber, studentId, studentName, shiftName)
        _selectedSeatForAction.value = null
        _uiToastMessage.value = "Seat $seatNumber assigned to $studentName"
    }

    fun releaseSeat(seatNumber: String) {
        repository.releaseSeat(seatNumber)
        _selectedSeatForAction.value = null
        _uiToastMessage.value = "Seat $seatNumber is now available"
    }

    fun toggleSeatMaintenance(seatNumber: String) {
        repository.toggleSeatMaintenance(seatNumber)
        _selectedSeatForAction.value = null
        _uiToastMessage.value = "Seat $seatNumber status updated"
    }

    fun markAttendance(studentId: String, status: AttendanceStatus, targetDate: String = _selectedAttendanceDate.value) {
        if (!isAttendanceDateEditable(targetDate)) {
            _uiToastMessage.value = "Attendance for $targetDate is locked. Historical records older than 2 days cannot be modified."
            return
        }
        repository.markAttendance(studentId, status, targetDate)
    }

    fun markAllPresent(targetDate: String = _selectedAttendanceDate.value) {
        if (!isAttendanceDateEditable(targetDate)) {
            _uiToastMessage.value = "Attendance for $targetDate is locked (older than 2 days)."
            return
        }
        repository.markAllPresent(targetDate)
        val daysAgo = getAttendanceDaysAgo(targetDate)
        val label = when (daysAgo) {
            0 -> "today"
            1 -> "yesterday"
            2 -> "day before yesterday"
            else -> targetDate
        }
        _uiToastMessage.value = "All active students marked Present for $label!"
    }

    fun collectFee(
        studentId: String,
        amount: Int,
        discount: Int,
        paymentMethod: PaymentMethod,
        notes: String,
        context: android.content.Context? = null,
        sendWhatsAppReceipt: Boolean = false
    ) {
        val payment = repository.collectFee(studentId, amount, discount, paymentMethod, notes)
        _showCollectFeeDialog.value = null
        if (payment != null) {
            _activeReceipt.value = payment
            _uiToastMessage.value = "₹$amount collected successfully! Receipt generated."
            if (sendWhatsAppReceipt && context != null) {
                sendStudentReceiptWhatsApp(context, payment)
            }
        }
    }

    fun addExpense(
        category: ExpenseCategory,
        title: String,
        amount: Int,
        paymentMethod: PaymentMethod,
        description: String
    ) {
        repository.addExpense(category, title, amount, paymentMethod, description)
        _showAddExpenseDialog.value = false
        _uiToastMessage.value = "₹$amount expense logged for $title"
    }

    fun approveRequest(requestId: String) {
        repository.approveRegistrationRequest(requestId)
        _uiToastMessage.value = "Registration request approved!"
    }

    fun rejectRequest(requestId: String) {
        repository.rejectRegistrationRequest(requestId)
        _uiToastMessage.value = "Registration request rejected"
    }

    fun updateShift(shift: Shift) {
        repository.updateShift(shift)
        _uiToastMessage.value = "Shift ${shift.name} updated successfully!"
    }

    fun deleteShift(shiftId: String) {
        val success = repository.deleteShift(shiftId)
        if (success) {
            _uiToastMessage.value = "Shift deleted successfully!"
        }
    }

    fun createShift(name: String, startTime: String, endTime: String, defaultPrice: Int) {
        repository.createShift(name, startTime, endTime, defaultPrice)
        _uiToastMessage.value = "Custom shift $name created successfully!"
    }

    fun renameSeat(oldSeatNumber: String, newSeatName: String) {
        val success = repository.renameSeat(oldSeatNumber, newSeatName)
        if (success) {
            _uiToastMessage.value = "Seat '$oldSeatNumber' renamed to '$newSeatName'!"
        }
    }

    // Authentication actions
    fun login(emailOrPhone: String, name: String = "Ratnesh Ankit"): Boolean {
        val success = repository.login(emailOrPhone, name)
        if (success) {
            _uiToastMessage.value = "Welcome back, ${repository.ownerProfile.value.fullName}!"
        }
        return success
    }

    fun requestLoginOtp(identifier: String): com.example.data.repository.LibraryRepository.OtpDispatchResult {
        val result = repository.requestLoginOtp(identifier)
        if (result.isSuccess) {
            _uiToastMessage.value = "✅ Verification code dispatched to ${result.targetEmail}."
        } else {
            _uiToastMessage.value = result.message
        }
        return result
    }

    fun sendOtp(identifier: String, viaEmail: Boolean = false): String {
        val result = requestLoginOtp(identifier)
        return result.otpCode
    }

    fun verifyOtpAndLogin(identifier: String, otp: String): Boolean {
        // Secret Super Admin Authentication check (Invisible to normal users)
        if (platformRepository.isSuperAdminCredentials(identifier, otp)) {
            platformRepository.authenticateSuperAdminDirectly()
            _uiToastMessage.value = "👑 Welcome back, Platform Administrator!"
            return true
        }

        val success = repository.verifyOtpAndLogin(identifier, otp)
        if (success) {
            _uiToastMessage.value = "OTP Verified! Welcome to your Library portal."
        } else {
            _uiToastMessage.value = "Invalid OTP code. Please enter the valid OTP code."
        }
        return success
    }

    fun sendPasswordResetOtp(identifier: String, preferEmail: Boolean = false): Triple<Boolean, String, String> {
        val res = repository.sendPasswordResetOtp(identifier, preferEmail)
        _uiToastMessage.value = if (res.first) {
            "✅ Verification code dispatched to your email. Please check your inbox."
        } else {
            res.second
        }
        return res
    }

    fun verifyPasswordResetOtp(enteredOtp: String): Pair<Boolean, String> {
        val res = repository.verifyPasswordResetOtp(enteredOtp)
        _uiToastMessage.value = res.second
        return res
    }

    fun resetAccountPassword(newPassword: String): Pair<Boolean, String> {
        val res = repository.resetAccountPassword(newPassword)
        _uiToastMessage.value = res.second
        return res
    }

    fun loginWithPassword(phoneOrEmail: String, password: String): Boolean {
        // Secret Super Admin Authentication check (Invisible to normal users)
        if (platformRepository.isSuperAdminCredentials(phoneOrEmail, password)) {
            platformRepository.authenticateSuperAdminDirectly()
            _uiToastMessage.value = "👑 Welcome back, Platform Administrator!"
            return true
        }

        val success = repository.loginWithPassword(phoneOrEmail, password)
        if (success) {
            _uiToastMessage.value = "Welcome back, ${repository.ownerProfile.value.fullName}!"
        } else {
            _uiToastMessage.value = "Account not found or password incorrect. Please check your credentials or register a new library."
        }
        return success
    }

    fun logout() {
        repository.logout()
        _uiToastMessage.value = "Logged out successfully"
    }

    fun registerOwner(fullName: String, phone: String, email: String, libraryName: String) {
        repository.registerOwner(fullName, phone, email, libraryName)
        _uiToastMessage.value = "Welcome, $fullName! Your library is ready."
    }

    fun isAccountAlreadyRegistered(phone: String, email: String): Boolean {
        return repository.isAccountAlreadyRegistered(phone, email)
    }

    fun registerFullLibrary(
        ownerName: String,
        phone: String,
        whatsapp: String,
        email: String,
        password: String,
        libraryName: String,
        contactNumber: String,
        libraryEmail: String,
        address: String,
        location: String,
        city: String = "",
        state: String = "",
        pincode: String = "",
        seatCapacity: Int,
        shifts: List<Shift>? = null
    ): Boolean {
        val success = repository.registerFullLibrary(
            ownerName = ownerName,
            phone = phone,
            whatsapp = whatsapp,
            email = email,
            password = password,
            libraryName = libraryName,
            contactNumber = contactNumber,
            libraryEmail = libraryEmail,
            address = address,
            location = location,
            city = city,
            state = state,
            pincode = pincode,
            seatCapacity = seatCapacity,
            customShifts = shifts
        )
        if (success) {
            _uiToastMessage.value = "🎉 Registration complete! Welcome $ownerName to $libraryName."
        } else {
            _uiToastMessage.value = "⚠️ An account with this phone number or email is already registered. Only 1 account is permitted. Please log in or contact the platform owner."
        }
        return success
    }

    fun toggleAccountSuspension(accountId: String, isSuspended: Boolean, reason: String = ""): Boolean {
        val success = platformRepository.toggleAccountSuspension(accountId, isSuspended, reason)
        if (success) {
            val currentActiveOwner = repository.ownerProfile.value
            if (currentActiveOwner.userId == accountId ||
                currentActiveOwner.phone == accountId ||
                currentActiveOwner.email.equals(accountId, ignoreCase = true)
            ) {
                repository.updateSuspensionState(isSuspended, reason)
            }
            _uiToastMessage.value = if (isSuspended) "🚫 Library account deactivated successfully." else "✅ Library account reactivated successfully."
        }
        return success
    }

    fun deleteLibraryAccount(accountId: String): Boolean {
        val success = platformRepository.deleteLibraryAccount(accountId)
        if (success) {
            val currentActiveOwner = repository.ownerProfile.value
            if (currentActiveOwner.userId == accountId ||
                currentActiveOwner.phone == accountId ||
                currentActiveOwner.email.equals(accountId, ignoreCase = true)
            ) {
                repository.logout()
            }
            _uiToastMessage.value = "🗑️ Library account permanently deleted."
        } else {
            _uiToastMessage.value = "❌ Failed to delete account."
        }
        return success
    }

    // ==========================================
    // WHATSAPP FEE DUE REMINDER SYSTEM
    // (Available on 2nd / Premium & 3rd / Business Plans)
    // ==========================================

    fun generateOwnerWhatsAppDueAlertText(dueStudents: List<Student>, lib: Library, own: OwnerProfile): String {
        val totalDue = dueStudents.sumOf { it.dueAmount }
        val todayStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        val sb = StringBuilder()
        sb.append("🔔 *LIBRARY FEE DUE NOTIFICATION*\n")
        sb.append("🏛️ *${lib.name}*\n")
        sb.append("📅 Date: $todayStr\n\n")
        sb.append("Hello *${own.fullName}*, the following students have fee due dates today / pending dues:\n\n")

        dueStudents.forEachIndexed { index, student ->
            sb.append("${index + 1}. *${student.fullName}* (${student.studentCode})\n")
            sb.append("   • 💰 Due: *₹${student.dueAmount}* (Monthly: ₹${student.monthlyFee})\n")
            sb.append("   • 📅 Due Date: ${student.feeDueDate}\n")
            sb.append("   • 🪑 Seat: ${student.assignedSeatNumber.ifBlank { "Floating" }} | ${student.assignedShiftName}\n")
            sb.append("   • 📞 Contact: ${student.mobile}\n\n")
        }

        sb.append("📊 *Total Pending Collection:* ₹$totalDue across ${dueStudents.size} student(s)\n")
        sb.append("💳 *Library UPI ID:* ${lib.upiId}\n\n")
        sb.append("💡 _Powered by My Library Management System_")
        return sb.toString()
    }

    fun generateStudentWhatsAppReminderText(student: Student, lib: Library): String {
        val sb = StringBuilder()
        sb.append("🏛️ *${lib.name.uppercase()}*\n")
        sb.append("📢 *FEE PAYMENT REMINDER*\n\n")
        sb.append("Dear *${student.fullName}*,\n\n")
        sb.append("This is a friendly reminder that your library subscription fee of *₹${student.dueAmount}* is due for payment on *${student.feeDueDate}*.\n\n")
        sb.append("📌 *Student Pass Details:*\n")
        sb.append("• Student ID: *${student.studentCode}*\n")
        sb.append("• Course: ${student.course}\n")
        sb.append("• Shift: ${student.assignedShiftName}\n")
        sb.append("• Assigned Seat: ${if (student.assignedSeatNumber.isNotBlank()) "Seat " + student.assignedSeatNumber else "Floating Area"}\n")
        sb.append("• Due Amount: *₹${student.dueAmount}*\n\n")
        sb.append("💳 *Payment Options:*\n")
        sb.append("• Pay via UPI: *${lib.upiId}*\n")
        sb.append("• Or visit the library front desk.\n\n")
        sb.append("Kindly share the transaction screenshot after payment to receive your digital receipt.\n\n")
        sb.append("Best regards,\n")
        sb.append("*${lib.name} Management*\n")
        sb.append("📞 ${lib.phone}")
        return sb.toString()
    }

    fun launchWhatsApp(context: android.content.Context, rawPhone: String, message: String) {
        try {
            var cleanPhone = rawPhone.replace("+", "").replace(" ", "").replace("-", "").trim()
            if (cleanPhone.length == 10) {
                cleanPhone = "91$cleanPhone"
            }
            val encodedMessage = java.net.URLEncoder.encode(message, "UTF-8")
            val url = if (cleanPhone.isNotBlank()) {
                "https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMessage"
            } else {
                "https://api.whatsapp.com/send?text=$encodedMessage"
            }
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(android.content.Intent.EXTRA_TEXT, message)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(android.content.Intent.createChooser(shareIntent, "Send WhatsApp Reminder"))
            } catch (e2: Exception) {
                _uiToastMessage.value = "WhatsApp message prepared"
            }
        }
    }

    fun sendOwnerWhatsAppAlert(context: android.content.Context) {
        if (!hasFeature("whatsapp_fee_reminders")) {
            requestUpgrade("whatsapp_fee_reminders")
            return
        }
        val dueStudents = students.value.filter { it.dueAmount > 0 }
        if (dueStudents.isEmpty()) {
            _uiToastMessage.value = "No students have pending fee dues! 🎉"
            return
        }
        val own = ownerProfile.value
        val lib = library.value
        val msg = generateOwnerWhatsAppDueAlertText(dueStudents, lib, own)
        val targetPhone = own.whatsapp.ifBlank { own.phone }

        repository.logWhatsAppReminder(
            studentName = "${dueStudents.size} Due Students",
            targetPhone = targetPhone,
            dueAmount = dueStudents.sumOf { it.dueAmount },
            dueDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            messageType = "OWNER_ALERT"
        )

        launchWhatsApp(context, targetPhone, msg)
        _uiToastMessage.value = "Opening WhatsApp to notify owner (${own.fullName}) of ${dueStudents.size} fee dues!"
    }

    fun sendStudentWhatsAppReminder(context: android.content.Context, student: Student) {
        if (!hasFeature("whatsapp_fee_reminders")) {
            requestUpgrade("whatsapp_fee_reminders")
            return
        }
        val lib = library.value
        val msg = generateStudentWhatsAppReminderText(student, lib)
        val targetPhone = student.whatsapp.ifBlank { student.mobile }

        repository.logWhatsAppReminder(
            studentName = student.fullName,
            targetPhone = targetPhone,
            dueAmount = student.dueAmount,
            dueDate = student.feeDueDate,
            messageType = "STUDENT_REMINDER"
        )

        launchWhatsApp(context, targetPhone, msg)
        _uiToastMessage.value = "Opening WhatsApp fee reminder for ${student.fullName} (₹${student.dueAmount} due)"
    }

    fun generateStudentWelcomeWhatsAppText(student: Student, lib: Library): String {
        val sb = StringBuilder()
        sb.append("🎉 *WELCOME TO ${lib.name.uppercase()}*\n")
        sb.append("_Powered by Vidyara Library Management_\n\n")
        sb.append("Dear *${student.fullName}*,\n")
        sb.append("Your admission and workspace seat have been successfully confirmed! Here is your digital student pass:\n\n")
        sb.append("🪪 *STUDENT PASS DETAILS*\n")
        sb.append("• Student ID: *${student.studentCode}*\n")
        sb.append("• Course / Goal: ${student.course}\n")
        sb.append("• Shift: *${student.assignedShiftName}*\n")
        sb.append("• Assigned Seat: *${if (student.assignedSeatNumber.isNotBlank()) "Seat " + student.assignedSeatNumber else "Floating Seat"}*\n")
        sb.append("• Monthly Fee: ₹${student.monthlyFee}\n")
        if (student.dueAmount > 0) {
            sb.append("• Pending Due: ₹${student.dueAmount} (Due by: ${student.feeDueDate})\n")
        } else {
            sb.append("• Fee Status: ✅ Fully Paid\n")
        }
        sb.append("\n📍 *Library Location:*\n")
        sb.append("${lib.address}, ${lib.city}\n")
        sb.append("🕒 *Operating Hours:* ${lib.openingTime} - ${lib.closingTime}\n")
        sb.append("📞 *Helpdesk:* ${lib.phone}\n")
        if (lib.upiId.isNotBlank()) {
            sb.append("💳 *UPI ID for Fees:* ${lib.upiId}\n")
        }
        sb.append("\nWe wish you great success in your study journey! 🚀")
        return sb.toString()
    }

    fun sendStudentWelcomeWhatsApp(context: android.content.Context, student: Student) {
        val lib = library.value
        val msg = generateStudentWelcomeWhatsAppText(student, lib)
        val targetPhone = student.whatsapp.ifBlank { student.mobile }

        repository.logWhatsAppReminder(
            studentName = student.fullName,
            targetPhone = targetPhone,
            dueAmount = student.dueAmount,
            dueDate = student.feeDueDate,
            messageType = "WELCOME_PASS"
        )

        launchWhatsApp(context, targetPhone, msg)
        _uiToastMessage.value = "Opening WhatsApp to send welcome pass to ${student.fullName}!"
    }

    fun generateFeeReceiptWhatsAppText(payment: StudentPayment, lib: Library): String {
        val student = students.value.find { it.id == payment.studentId || it.fullName == payment.studentName }
        val sb = StringBuilder()
        sb.append("🏛️ *${lib.name.uppercase()}*\n")
        sb.append("📄 *FEE PAYMENT RECEIPT & INVOICE*\n")
        sb.append("--------------------------------------\n")
        sb.append("Receipt No: *${payment.receiptNumber}*\n")
        sb.append("Date: ${payment.paymentDate}\n\n")
        sb.append("👤 Student: *${payment.studentName}*\n")
        sb.append("🆔 Student ID: ${payment.studentCode}\n")
        sb.append("🪑 Assigned Seat: ${if (payment.seatNumber.isNotBlank()) "Seat " + payment.seatNumber else "Floating Area"}\n")
        sb.append("🕒 Shift: ${payment.shiftName}\n")
        sb.append("--------------------------------------\n")
        sb.append("💰 *Paid Amount: ₹${payment.amount}*\n")
        sb.append("💳 Payment Mode: ${payment.paymentMethod.name}\n")
        sb.append("🔗 Transaction ID: ${payment.transactionId}\n")
        if (payment.notes.isNotBlank()) {
            sb.append("📝 Description: ${payment.notes}\n")
        }
        if (student != null) {
            if (student.dueAmount > 0) {
                sb.append("⚠️ *Remaining Balance Due: ₹${student.dueAmount}*\n")
            } else {
                sb.append("✅ *Fee Status: Fully Cleared*\n")
            }
        }
        sb.append("--------------------------------------\n")
        sb.append("Thank you for your payment!\n")
        sb.append("📞 ${lib.phone} | 💳 UPI: ${lib.upiId}\n")
        sb.append("💡 _Digitally Verified & Generated by Vidyara_")
        return sb.toString()
    }

    fun sendStudentReceiptWhatsApp(context: android.content.Context, payment: StudentPayment) {
        val lib = library.value
        val student = students.value.find { it.id == payment.studentId || it.fullName == payment.studentName }
        val msg = generateFeeReceiptWhatsAppText(payment, lib)
        val targetPhone = student?.whatsapp?.ifBlank { student.mobile } ?: student?.mobile ?: ""

        repository.logWhatsAppReminder(
            studentName = payment.studentName,
            targetPhone = targetPhone,
            dueAmount = 0,
            dueDate = payment.paymentDate,
            messageType = "FEE_RECEIPT"
        )

        launchWhatsApp(context, targetPhone, msg)
        _uiToastMessage.value = "Opening WhatsApp to send receipt for ₹${payment.amount} to ${payment.studentName}!"
    }

    fun exportFinancialReport(context: android.content.Context) {
        if (!hasFeature("revenue_download")) {
            requestUpgrade("revenue_download")
            return
        }
        val lib = library.value
        val payList = payments.value
        val expList = expenses.value
        val stuList = students.value

        val totalRev = payList.sumOf { it.amount }
        val totalExp = expList.sumOf { it.amount }
        val netProf = totalRev - totalExp
        val totalDues = stuList.sumOf { it.dueAmount }

        val sb = StringBuilder()
        sb.append("========================================\n")
        sb.append("🏛️ ${lib.name.uppercase()} - FINANCIAL STATEMENT\n")
        sb.append("Generated on: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}\n")
        sb.append("Location: ${lib.address}, ${lib.city}\n")
        sb.append("========================================\n\n")

        sb.append("📊 EXECUTIVE FINANCIAL SUMMARY\n")
        sb.append("• Total Fee Collections: ₹$totalRev (${payList.size} transactions)\n")
        sb.append("• Total Operating Expenses: ₹$totalExp (${expList.size} logged)\n")
        sb.append("• Net Profit / Surplus: ₹$netProf\n")
        sb.append("• Outstanding Fee Dues: ₹$totalDues across ${stuList.count { it.dueAmount > 0 }} students\n")
        sb.append("• Total Enrolled Students: ${stuList.size}\n\n")

        sb.append("----------------------------------------\n")
        sb.append("💵 RECENT FEE COLLECTIONS:\n")
        sb.append("----------------------------------------\n")
        payList.take(25).forEach { p ->
            sb.append("• ${p.paymentDate} | ${p.receiptNumber} | ${p.studentName} | ₹${p.amount} (${p.paymentMethod.name})\n")
        }

        sb.append("\n----------------------------------------\n")
        sb.append("📉 RECENT EXPENSES:\n")
        sb.append("----------------------------------------\n")
        expList.take(25).forEach { e ->
            sb.append("• ${e.expenseDate} | ${e.category.name} | ${e.title} | ₹${e.amount} (${e.paymentMethod.name})\n")
        }

        sb.append("\n========================================\n")
        sb.append("💡 Generated by Vidyara Library Management Software\n")
        sb.append("========================================\n")

        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_SUBJECT, "${lib.name} - Financial Statement")
            putExtra(android.content.Intent.EXTRA_TEXT, sb.toString())
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(android.content.Intent.createChooser(shareIntent, "Export Financial Statement"))
            _uiToastMessage.value = "Financial statement ready to share / print!"
        } catch (e: Exception) {
            _uiToastMessage.value = "Unable to open share menu"
        }
    }

    fun lookupPincode(pincode: String, onResult: (city: String, state: String) -> Unit) {
        viewModelScope.launch {
            val result = repository.lookupPincode(pincode)
            if (result != null) {
                onResult(result.first, result.second)
            }
        }
    }
}
