package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class SavedLibraryAccount(
    val accountId: String,
    val ownerProfile: OwnerProfile,
    val library: Library,
    val branches: List<Branch>,
    val activeBranchId: String,
    val saasSubscription: SaaSSubscription,
    val shifts: List<Shift>,
    val libraryPlans: List<LibraryPlan>,
    val seats: List<Seat>,
    val students: List<Student>,
    val attendance: List<AttendanceRecord>,
    val payments: List<StudentPayment>,
    val expenses: List<Expense>,
    val registrationRequests: List<RegistrationRequest>,
    val auditLogs: List<AuditLog>,
    val saasPurchaseHistory: List<SaaSPurchaseRecord> = emptyList()
)

class LibraryAccountStorage(private val context: Context? = null) {

    private val ctx = context ?: com.example.LibraryApp.appContext
    private val prefs: SharedPreferences? = try {
        ctx?.getSharedPreferences("library_accounts_v2", Context.MODE_PRIVATE)
    } catch (e: Exception) {
        null
    }

    private val KEY_ACCOUNTS_MAP = "saved_accounts_json_map"
    private val KEY_LAST_LOGGED_IN_ID = "last_logged_in_account_id"

    fun getAllAccounts(): Map<String, SavedLibraryAccount> {
        val jsonStr = prefs?.getString(KEY_ACCOUNTS_MAP, null) ?: return emptyMap()
        val result = mutableMapOf<String, SavedLibraryAccount>()
        try {
            val rootObj = JSONObject(jsonStr)
            val keys = rootObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val accObj = rootObj.getJSONObject(key)
                val account = deserializeAccount(accObj)
                result[key] = account
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    fun saveAccount(account: SavedLibraryAccount) {
        try {
            val currentAccounts = getAllAccounts().toMutableMap()
            // Key by normalized phone and normalized email
            val phoneKey = normalizePhone(account.ownerProfile.phone)
            val emailKey = account.ownerProfile.email.trim().lowercase()

            val primaryKey = if (phoneKey.isNotBlank()) phoneKey else emailKey
            currentAccounts[primaryKey] = account
            if (emailKey.isNotBlank() && emailKey != primaryKey) {
                currentAccounts[emailKey] = account
            }

            val rootObj = JSONObject()
            // Save unique accounts
            val uniqueAccounts = currentAccounts.values.distinctBy { it.accountId }
            uniqueAccounts.forEach { acc ->
                val pKey = normalizePhone(acc.ownerProfile.phone)
                val eKey = acc.ownerProfile.email.trim().lowercase()
                val key = if (pKey.isNotBlank()) pKey else eKey
                rootObj.put(key, serializeAccount(acc))
                if (eKey.isNotBlank() && eKey != key) {
                    rootObj.put(eKey, serializeAccount(acc))
                }
            }

            prefs?.edit()
                ?.putString(KEY_ACCOUNTS_MAP, rootObj.toString())
                ?.apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteAccount(accountId: String): Boolean {
        try {
            val currentAccounts = getAllAccounts().toMutableMap()
            val target = currentAccounts.values.find {
                it.accountId == accountId ||
                it.ownerProfile.phone == accountId ||
                normalizePhone(it.ownerProfile.phone) == normalizePhone(accountId) ||
                it.ownerProfile.email.equals(accountId.trim(), ignoreCase = true)
            } ?: return false

            val pKey = normalizePhone(target.ownerProfile.phone)
            val eKey = target.ownerProfile.email.trim().lowercase()

            currentAccounts.remove(pKey)
            currentAccounts.remove(eKey)

            val remainingUnique = currentAccounts.values.filter { it.accountId != target.accountId }
            val rootObj = JSONObject()
            remainingUnique.forEach { acc ->
                val pk = normalizePhone(acc.ownerProfile.phone)
                val ek = acc.ownerProfile.email.trim().lowercase()
                val k = if (pk.isNotBlank()) pk else ek
                rootObj.put(k, serializeAccount(acc))
                if (ek.isNotBlank() && ek != k) {
                    rootObj.put(ek, serializeAccount(acc))
                }
            }

            val editor = prefs?.edit() ?: return false
            editor.putString(KEY_ACCOUNTS_MAP, rootObj.toString())
            if (prefs.getString(KEY_LAST_LOGGED_IN_ID, "") == target.accountId) {
                editor.remove(KEY_LAST_LOGGED_IN_ID)
            }
            editor.apply()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun findAccount(query: String): SavedLibraryAccount? {
        val accounts = getAllAccounts()
        val normQuery = query.trim().lowercase()
        val normPhone = normalizePhone(query)

        // Try exact match in map
        if (normPhone.isNotBlank() && accounts.containsKey(normPhone)) {
            return accounts[normPhone]
        }
        if (accounts.containsKey(normQuery)) {
            return accounts[normQuery]
        }

        // Search through all unique accounts
        return accounts.values.firstOrNull { acc ->
            val pNorm = normalizePhone(acc.ownerProfile.phone)
            val pWhatsappNorm = normalizePhone(acc.ownerProfile.whatsapp)
            val eNorm = acc.ownerProfile.email.trim().lowercase()

            (normPhone.isNotBlank() && (pNorm == normPhone || pWhatsappNorm == normPhone || pNorm.endsWith(normPhone) || normPhone.endsWith(pNorm))) ||
            (normQuery.isNotBlank() && eNorm == normQuery) ||
            (acc.ownerProfile.fullName.equals(query.trim(), ignoreCase = true))
        }
    }

    fun getLastLoggedInAccountId(): String? {
        return prefs?.getString(KEY_LAST_LOGGED_IN_ID, null)
    }

    fun setLastLoggedInAccountId(id: String?) {
        prefs?.edit()?.putString(KEY_LAST_LOGGED_IN_ID, id)?.apply()
    }

    fun clearAllData() {
        prefs?.edit()?.clear()?.apply()
    }

    fun normalizePhone(phone: String): String {
        return phone.replace("+", "")
            .replace(" ", "")
            .replace("-", "")
            .replace("(", "")
            .replace(")", "")
            .trim()
    }

    // ==========================================
    // SERIALIZATION
    // ==========================================

    fun serializeAccount(acc: SavedLibraryAccount): JSONObject {
        val obj = JSONObject()
        obj.put("accountId", acc.accountId)
        obj.put("ownerProfile", serializeOwner(acc.ownerProfile))
        obj.put("library", serializeLibrary(acc.library))
        obj.put("branches", serializeBranches(acc.branches))
        obj.put("activeBranchId", acc.activeBranchId)
        obj.put("saasSubscription", serializeSaaS(acc.saasSubscription))
        obj.put("shifts", serializeShifts(acc.shifts))
        obj.put("libraryPlans", serializePlans(acc.libraryPlans))
        obj.put("seats", serializeSeats(acc.seats))
        obj.put("students", serializeStudents(acc.students))
        obj.put("attendance", serializeAttendance(acc.attendance))
        obj.put("payments", serializePayments(acc.payments))
        obj.put("expenses", serializeExpenses(acc.expenses))
        obj.put("registrationRequests", serializeRequests(acc.registrationRequests))
        obj.put("auditLogs", serializeAuditLogs(acc.auditLogs))
        obj.put("saasPurchaseHistory", serializePurchases(acc.saasPurchaseHistory))
        return obj
    }

    private fun serializePurchases(list: List<SaaSPurchaseRecord>): JSONArray {
        val arr = JSONArray()
        list.forEach { p ->
            val o = JSONObject().apply {
                put("id", p.id)
                put("timestamp", p.timestamp)
                put("productName", p.productName)
                put("amount", p.amount)
                put("status", p.status)
                put("razorpayPaymentId", p.razorpayPaymentId)
                put("invoiceNumber", p.invoiceNumber)
                put("billingPeriod", p.billingPeriod)
                put("branchCount", p.branchCount)
            }
            arr.put(o)
        }
        return arr
    }

    private fun serializeOwner(o: OwnerProfile): JSONObject {
        return JSONObject().apply {
            put("id", o.id)
            put("userId", o.userId)
            put("fullName", o.fullName)
            put("phone", o.phone)
            put("whatsapp", o.whatsapp)
            put("email", o.email)
            put("avatarUrl", o.avatarUrl)
            put("password", o.password)
            put("isSuspended", o.isSuspended)
            put("suspensionReason", o.suspensionReason)
        }
    }

    private fun serializeLibrary(l: Library): JSONObject {
        return JSONObject().apply {
            put("id", l.id)
            put("ownerId", l.ownerId)
            put("name", l.name)
            put("logoUrl", l.logoUrl)
            put("phone", l.phone)
            put("email", l.email)
            put("address", l.address)
            put("location", l.location)
            put("city", l.city)
            put("state", l.state)
            put("pincode", l.pincode)
            put("totalSeats", l.totalSeats)
            put("openingTime", l.openingTime)
            put("closingTime", l.closingTime)
            put("registrationToken", l.registrationToken)
            put("upiId", l.upiId)
        }
    }

    private fun serializeBranches(list: List<Branch>): JSONArray {
        val arr = JSONArray()
        list.forEach { b ->
            arr.put(JSONObject().apply {
                put("id", b.id)
                put("libraryId", b.libraryId)
                put("name", b.name)
                put("code", b.code)
                put("address", b.address)
                put("phone", b.phone)
                put("city", b.city)
                put("state", b.state)
                put("pincode", b.pincode)
                put("totalSeats", b.totalSeats)
                put("openingTime", b.openingTime)
                put("closingTime", b.closingTime)
                put("upiId", b.upiId)
                put("isActive", b.isActive)
                put("isPrimary", b.isPrimary)
            })
        }
        return arr
    }

    private fun serializeSaaS(s: SaaSSubscription): JSONObject {
        return JSONObject().apply {
            put("planType", s.planType.name)
            put("billingPeriod", s.billingPeriod.name)
            put("startDate", s.startDate)
            put("endDate", s.endDate)
            put("isActive", s.isActive)
        }
    }

    private fun serializeShifts(list: List<Shift>): JSONArray {
        val arr = JSONArray()
        list.forEach { sh ->
            arr.put(JSONObject().apply {
                put("id", sh.id)
                put("libraryId", sh.libraryId)
                put("branchId", sh.branchId)
                put("name", sh.name)
                put("startTime", sh.startTime)
                put("endTime", sh.endTime)
                put("defaultPrice", sh.defaultPrice)
                put("capacity", sh.capacity)
                put("isActive", sh.isActive)
            })
        }
        return arr
    }

    private fun serializePlans(list: List<LibraryPlan>): JSONArray {
        val arr = JSONArray()
        list.forEach { p ->
            arr.put(JSONObject().apply {
                put("id", p.id)
                put("libraryId", p.libraryId)
                put("branchId", p.branchId)
                put("name", p.name)
                put("durationMonths", p.durationMonths)
                put("price", p.price)
                put("shiftName", p.shiftName)
                put("seatType", p.seatType.name)
                put("description", p.description)
            })
        }
        return arr
    }

    private fun serializeSeats(list: List<Seat>): JSONArray {
        val arr = JSONArray()
        list.forEach { s ->
            arr.put(JSONObject().apply {
                put("id", s.id)
                put("branchId", s.branchId)
                put("floorId", s.floorId)
                put("roomId", s.roomId)
                put("seatNumber", s.seatNumber)
                put("seatType", s.seatType.name)
                put("status", s.status.name)
                put("assignedStudentId", s.assignedStudentId ?: "")
                put("assignedStudentName", s.assignedStudentName ?: "")
                put("assignedShiftId", s.assignedShiftId ?: "")
                put("assignedShiftName", s.assignedShiftName ?: "")
                put("expiryDate", s.expiryDate ?: "")
            })
        }
        return arr
    }

    private fun serializeStudents(list: List<Student>): JSONArray {
        val arr = JSONArray()
        list.forEach { st ->
            arr.put(JSONObject().apply {
                put("id", st.id)
                put("libraryId", st.libraryId)
                put("branchId", st.branchId)
                put("studentCode", st.studentCode)
                put("fullName", st.fullName)
                put("photoUrl", st.photoUrl)
                put("mobile", st.mobile)
                put("whatsapp", st.whatsapp)
                put("email", st.email)
                put("gender", st.gender)
                put("dateOfBirth", st.dateOfBirth)
                put("address", st.address)
                put("guardianName", st.guardianName)
                put("guardianPhone", st.guardianPhone)
                put("course", st.course)
                put("college", st.college)
                put("joiningDate", st.joiningDate)
                put("feeDueDate", st.feeDueDate)
                put("status", st.status.name)
                put("assignedSeatNumber", st.assignedSeatNumber)
                put("assignedShiftName", st.assignedShiftName)
                put("monthlyFee", st.monthlyFee)
                put("dueAmount", st.dueAmount)
                put("notes", st.notes)
            })
        }
        return arr
    }

    private fun serializeAttendance(list: List<AttendanceRecord>): JSONArray {
        val arr = JSONArray()
        list.forEach { a ->
            arr.put(JSONObject().apply {
                put("id", a.id)
                put("libraryId", a.libraryId)
                put("branchId", a.branchId)
                put("studentId", a.studentId)
                put("studentName", a.studentName)
                put("studentCode", a.studentCode)
                put("shiftName", a.shiftName)
                put("seatNumber", a.seatNumber)
                put("attendanceDate", a.attendanceDate)
                put("checkInTime", a.checkInTime ?: "")
                put("checkOutTime", a.checkOutTime ?: "")
                put("status", a.status.name)
            })
        }
        return arr
    }

    private fun serializePayments(list: List<StudentPayment>): JSONArray {
        val arr = JSONArray()
        list.forEach { p ->
            arr.put(JSONObject().apply {
                put("id", p.id)
                put("libraryId", p.libraryId)
                put("branchId", p.branchId)
                put("studentId", p.studentId)
                put("studentName", p.studentName)
                put("studentCode", p.studentCode)
                put("amount", p.amount)
                put("discount", p.discount)
                put("paymentDate", p.paymentDate)
                put("paymentMethod", p.paymentMethod.name)
                put("transactionId", p.transactionId)
                put("receiptNumber", p.receiptNumber)
                put("notes", p.notes)
                put("shiftName", p.shiftName)
                put("seatNumber", p.seatNumber)
            })
        }
        return arr
    }

    private fun serializeExpenses(list: List<Expense>): JSONArray {
        val arr = JSONArray()
        list.forEach { e ->
            arr.put(JSONObject().apply {
                put("id", e.id)
                put("libraryId", e.libraryId)
                put("branchId", e.branchId)
                put("category", e.category.name)
                put("title", e.title)
                put("amount", e.amount)
                put("expenseDate", e.expenseDate)
                put("paymentMethod", e.paymentMethod.name)
                put("description", e.description)
            })
        }
        return arr
    }

    private fun serializeRequests(list: List<RegistrationRequest>): JSONArray {
        val arr = JSONArray()
        list.forEach { r ->
            arr.put(JSONObject().apply {
                put("id", r.id)
                put("studentName", r.studentName)
                put("mobile", r.mobile)
                put("email", r.email)
                put("course", r.course)
                put("requestedShift", r.requestedShift)
                put("preferredSeat", r.preferredSeat)
                put("requestDate", r.requestDate)
                put("status", r.status)
            })
        }
        return arr
    }

    private fun serializeAuditLogs(list: List<AuditLog>): JSONArray {
        val arr = JSONArray()
        list.forEach { log ->
            arr.put(JSONObject().apply {
                put("id", log.id)
                put("action", log.action)
                put("entity", log.entity)
                put("details", log.details)
                put("timestamp", log.timestamp)
                put("user", log.user)
            })
        }
        return arr
    }

    // ==========================================
    // DESERIALIZATION
    // ==========================================

    fun deserializeAccount(obj: JSONObject): SavedLibraryAccount {
        return SavedLibraryAccount(
            accountId = obj.optString("accountId", UUID.randomUUID().toString()),
            ownerProfile = deserializeOwner(obj.optJSONObject("ownerProfile")),
            library = deserializeLibrary(obj.optJSONObject("library")),
            branches = deserializeBranches(obj.optJSONArray("branches")),
            activeBranchId = obj.optString("activeBranchId", "branch_01"),
            saasSubscription = deserializeSaaS(obj.optJSONObject("saasSubscription")),
            shifts = deserializeShifts(obj.optJSONArray("shifts")),
            libraryPlans = deserializePlans(obj.optJSONArray("libraryPlans")),
            seats = deserializeSeats(obj.optJSONArray("seats")),
            students = deserializeStudents(obj.optJSONArray("students")),
            attendance = deserializeAttendance(obj.optJSONArray("attendance")),
            payments = deserializePayments(obj.optJSONArray("payments")),
            expenses = deserializeExpenses(obj.optJSONArray("expenses")),
            registrationRequests = deserializeRequests(obj.optJSONArray("registrationRequests")),
            auditLogs = deserializeAuditLogs(obj.optJSONArray("auditLogs")),
            saasPurchaseHistory = deserializePurchases(obj.optJSONArray("saasPurchaseHistory"))
        )
    }

    private fun deserializePurchases(arr: JSONArray?): List<SaaSPurchaseRecord> {
        if (arr == null || arr.length() == 0) return emptyList()
        val list = mutableListOf<SaaSPurchaseRecord>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                SaaSPurchaseRecord(
                    id = o.optString("id", UUID.randomUUID().toString()),
                    timestamp = o.optString("timestamp", ""),
                    productName = o.optString("productName", "Vidyara Subscription"),
                    amount = o.optInt("amount", 0),
                    status = o.optString("status", "SUCCESS"),
                    razorpayPaymentId = o.optString("razorpayPaymentId", ""),
                    invoiceNumber = o.optString("invoiceNumber", ""),
                    billingPeriod = o.optString("billingPeriod", "Monthly"),
                    branchCount = o.optInt("branchCount", 1)
                )
            )
        }
        return list
    }

    private fun deserializeOwner(o: JSONObject?): OwnerProfile {
        if (o == null) return OwnerProfile()
        return OwnerProfile(
            id = o.optString("id", UUID.randomUUID().toString()),
            userId = o.optString("userId", "usr_owner_01"),
            fullName = o.optString("fullName", "Library Owner"),
            phone = o.optString("phone", "+91 9876543210"),
            whatsapp = o.optString("whatsapp", o.optString("phone", "+91 9876543210")),
            email = o.optString("email", "owner@library.com"),
            avatarUrl = o.optString("avatarUrl", ""),
            password = o.optString("password", "admin123"),
            isSuspended = o.optBoolean("isSuspended", false),
            suspensionReason = o.optString("suspensionReason", "")
        )
    }

    private fun deserializeLibrary(l: JSONObject?): Library {
        if (l == null) return Library()
        return Library(
            id = l.optString("id", "lib_01"),
            ownerId = l.optString("ownerId", "owner_01"),
            name = l.optString("name", "My Study Point & Library"),
            logoUrl = l.optString("logoUrl", ""),
            phone = l.optString("phone", "+91 9876543210"),
            email = l.optString("email", "contact@library.com"),
            address = l.optString("address", "Main Road"),
            location = l.optString("location", "City Center"),
            city = l.optString("city", "Delhi NCR"),
            state = l.optString("state", "UP"),
            pincode = l.optString("pincode", "201301"),
            totalSeats = l.optInt("totalSeats", 60),
            openingTime = l.optString("openingTime", "06:00 AM"),
            closingTime = l.optString("closingTime", "11:00 PM"),
            registrationToken = l.optString("registrationToken", "reg_token_01"),
            upiId = l.optString("upiId", "library@upi")
        )
    }

    private fun deserializeBranches(arr: JSONArray?): List<Branch> {
        if (arr == null || arr.length() == 0) {
            return listOf(
                Branch(
                    id = "branch_01",
                    libraryId = "lib_01",
                    name = "Knowledge Park III",
                    code = "BR-01",
                    address = "Main Road",
                    phone = "+91 9876543210",
                    city = "Greater Noida",
                    state = "Uttar Pradesh",
                    pincode = "201310",
                    totalSeats = 60,
                    openingTime = "06:00 AM",
                    closingTime = "11:00 PM",
                    upiId = "saraswati.lib@okhdfcbank",
                    isActive = true,
                    isPrimary = true
                )
            )
        }
        val list = mutableListOf<Branch>()
        for (i in 0 until arr.length()) {
            val b = arr.getJSONObject(i)
            list.add(
                Branch(
                    id = b.optString("id", "branch_${i + 1}"),
                    libraryId = b.optString("libraryId", "lib_01"),
                    name = b.optString("name", "Branch ${i + 1}"),
                    code = b.optString("code", "BR-0${i + 1}"),
                    address = b.optString("address", ""),
                    phone = b.optString("phone", ""),
                    city = b.optString("city", "Greater Noida"),
                    state = b.optString("state", "Uttar Pradesh"),
                    pincode = b.optString("pincode", "201310"),
                    totalSeats = b.optInt("totalSeats", 60),
                    openingTime = b.optString("openingTime", "06:00 AM"),
                    closingTime = b.optString("closingTime", "11:00 PM"),
                    upiId = b.optString("upiId", "saraswati.lib@okhdfcbank"),
                    isActive = b.optBoolean("isActive", true),
                    isPrimary = b.optBoolean("isPrimary", i == 0)
                )
            )
        }
        return list
    }

    private fun deserializeSaaS(s: JSONObject?): SaaSSubscription {
        if (s == null) return SaaSSubscription()
        val planName = s.optString("planType", SaaSPlanType.FREE.name)
        val periodName = s.optString("billingPeriod", BillingPeriod.MONTHLY.name)
        return SaaSSubscription(
            planType = try { SaaSPlanType.valueOf(planName) } catch (e: Exception) { SaaSPlanType.FREE },
            billingPeriod = try { BillingPeriod.valueOf(periodName) } catch (e: Exception) { BillingPeriod.MONTHLY },
            startDate = s.optString("startDate", "2026-08-01"),
            endDate = s.optString("endDate", "2099-12-31"),
            isActive = s.optBoolean("isActive", true)
        )
    }

    private fun deserializeShifts(arr: JSONArray?): List<Shift> {
        if (arr == null || arr.length() == 0) {
            return listOf(
                Shift(name = "Morning Shift", startTime = "06:00 AM", endTime = "12:00 PM", defaultPrice = 600, capacity = 60),
                Shift(name = "Afternoon Shift", startTime = "12:00 PM", endTime = "05:00 PM", defaultPrice = 550, capacity = 60),
                Shift(name = "Evening Shift", startTime = "05:00 PM", endTime = "11:00 PM", defaultPrice = 650, capacity = 60),
                Shift(name = "Full Day (24x7)", startTime = "06:00 AM", endTime = "11:00 PM", defaultPrice = 1200, capacity = 60)
            )
        }
        val list = mutableListOf<Shift>()
        for (i in 0 until arr.length()) {
            val sh = arr.getJSONObject(i)
            list.add(
                Shift(
                    id = sh.optString("id", UUID.randomUUID().toString()),
                    libraryId = sh.optString("libraryId", "lib_01"),
                    branchId = sh.optString("branchId", "branch_01"),
                    name = sh.optString("name", "Shift ${i + 1}"),
                    startTime = sh.optString("startTime", "06:00 AM"),
                    endTime = sh.optString("endTime", "12:00 PM"),
                    defaultPrice = sh.optInt("defaultPrice", 600),
                    capacity = sh.optInt("capacity", 60),
                    isActive = sh.optBoolean("isActive", true)
                )
            )
        }
        return list
    }

    private fun deserializePlans(arr: JSONArray?): List<LibraryPlan> {
        if (arr == null || arr.length() == 0) return emptyList()
        val list = mutableListOf<LibraryPlan>()
        for (i in 0 until arr.length()) {
            val p = arr.getJSONObject(i)
            val stName = p.optString("seatType", SeatType.FIXED.name)
            list.add(
                LibraryPlan(
                    id = p.optString("id", UUID.randomUUID().toString()),
                    libraryId = p.optString("libraryId", "lib_01"),
                    branchId = p.optString("branchId", "branch_01"),
                    name = p.optString("name", "Plan"),
                    durationMonths = p.optInt("durationMonths", 1),
                    price = p.optInt("price", 600),
                    shiftName = p.optString("shiftName", "Morning Shift"),
                    seatType = try { SeatType.valueOf(stName) } catch (e: Exception) { SeatType.FIXED },
                    description = p.optString("description", "")
                )
            )
        }
        return list
    }

    private fun deserializeSeats(arr: JSONArray?): List<Seat> {
        if (arr == null || arr.length() == 0) return emptyList()
        val list = mutableListOf<Seat>()
        for (i in 0 until arr.length()) {
            val s = arr.getJSONObject(i)
            val typeStr = s.optString("seatType", SeatType.FIXED.name)
            val statusStr = s.optString("status", SeatStatus.AVAILABLE.name)
            list.add(
                Seat(
                    id = s.optString("id", UUID.randomUUID().toString()),
                    branchId = s.optString("branchId", "branch_01"),
                    floorId = s.optString("floorId", "floor_01"),
                    roomId = s.optString("roomId", "room_01"),
                    seatNumber = s.optString("seatNumber", "A-${i + 1}"),
                    seatType = try { SeatType.valueOf(typeStr) } catch (e: Exception) { SeatType.FIXED },
                    status = try { SeatStatus.valueOf(statusStr) } catch (e: Exception) { SeatStatus.AVAILABLE },
                    assignedStudentId = s.optString("assignedStudentId").takeIf { it.isNotBlank() },
                    assignedStudentName = s.optString("assignedStudentName").takeIf { it.isNotBlank() },
                    assignedShiftId = s.optString("assignedShiftId").takeIf { it.isNotBlank() },
                    assignedShiftName = s.optString("assignedShiftName").takeIf { it.isNotBlank() },
                    expiryDate = s.optString("expiryDate").takeIf { it.isNotBlank() }
                )
            )
        }
        return list
    }

    private fun deserializeStudents(arr: JSONArray?): List<Student> {
        if (arr == null || arr.length() == 0) return emptyList()
        val list = mutableListOf<Student>()
        for (i in 0 until arr.length()) {
            val st = arr.getJSONObject(i)
            val statusStr = st.optString("status", StudentStatus.ACTIVE.name)
            list.add(
                Student(
                    id = st.optString("id", UUID.randomUUID().toString()),
                    libraryId = st.optString("libraryId", "lib_01"),
                    branchId = st.optString("branchId", "branch_01"),
                    studentCode = st.optString("studentCode", "STU-001"),
                    fullName = st.optString("fullName", "Student"),
                    photoUrl = st.optString("photoUrl", ""),
                    mobile = st.optString("mobile", ""),
                    whatsapp = st.optString("whatsapp", ""),
                    email = st.optString("email", ""),
                    gender = st.optString("gender", "Male"),
                    dateOfBirth = st.optString("dateOfBirth", "2001-01-01"),
                    address = st.optString("address", ""),
                    guardianName = st.optString("guardianName", ""),
                    guardianPhone = st.optString("guardianPhone", ""),
                    course = st.optString("course", "Competitive Exams"),
                    college = st.optString("college", ""),
                    joiningDate = st.optString("joiningDate", "2026-08-01"),
                    feeDueDate = st.optString("feeDueDate", "2026-08-28"),
                    status = try { StudentStatus.valueOf(statusStr) } catch (e: Exception) { StudentStatus.ACTIVE },
                    assignedSeatNumber = st.optString("assignedSeatNumber", ""),
                    assignedShiftName = st.optString("assignedShiftName", "Morning Shift"),
                    monthlyFee = st.optInt("monthlyFee", 600),
                    dueAmount = st.optInt("dueAmount", 0),
                    notes = st.optString("notes", "")
                )
            )
        }
        return list
    }

    private fun deserializeAttendance(arr: JSONArray?): List<AttendanceRecord> {
        if (arr == null || arr.length() == 0) return emptyList()
        val list = mutableListOf<AttendanceRecord>()
        for (i in 0 until arr.length()) {
            val a = arr.getJSONObject(i)
            val stName = a.optString("status", AttendanceStatus.PRESENT.name)
            list.add(
                AttendanceRecord(
                    id = a.optString("id", UUID.randomUUID().toString()),
                    libraryId = a.optString("libraryId", "lib_01"),
                    branchId = a.optString("branchId", "branch_01"),
                    studentId = a.optString("studentId", ""),
                    studentName = a.optString("studentName", ""),
                    studentCode = a.optString("studentCode", ""),
                    shiftName = a.optString("shiftName", ""),
                    seatNumber = a.optString("seatNumber", ""),
                    attendanceDate = a.optString("attendanceDate", ""),
                    checkInTime = a.optString("checkInTime").takeIf { it.isNotBlank() },
                    checkOutTime = a.optString("checkOutTime").takeIf { it.isNotBlank() },
                    status = try { AttendanceStatus.valueOf(stName) } catch (e: Exception) { AttendanceStatus.PRESENT }
                )
            )
        }
        return list
    }

    private fun deserializePayments(arr: JSONArray?): List<StudentPayment> {
        if (arr == null || arr.length() == 0) return emptyList()
        val list = mutableListOf<StudentPayment>()
        for (i in 0 until arr.length()) {
            val p = arr.getJSONObject(i)
            val methodStr = p.optString("paymentMethod", PaymentMethod.UPI.name)
            list.add(
                StudentPayment(
                    id = p.optString("id", UUID.randomUUID().toString()),
                    libraryId = p.optString("libraryId", "lib_01"),
                    branchId = p.optString("branchId", "branch_01"),
                    studentId = p.optString("studentId", ""),
                    studentName = p.optString("studentName", ""),
                    studentCode = p.optString("studentCode", ""),
                    amount = p.optInt("amount", 0),
                    discount = p.optInt("discount", 0),
                    paymentDate = p.optString("paymentDate", ""),
                    paymentMethod = try { PaymentMethod.valueOf(methodStr) } catch (e: Exception) { PaymentMethod.UPI },
                    transactionId = p.optString("transactionId", ""),
                    receiptNumber = p.optString("receiptNumber", ""),
                    notes = p.optString("notes", ""),
                    shiftName = p.optString("shiftName", ""),
                    seatNumber = p.optString("seatNumber", "")
                )
            )
        }
        return list
    }

    private fun deserializeExpenses(arr: JSONArray?): List<Expense> {
        if (arr == null || arr.length() == 0) return emptyList()
        val list = mutableListOf<Expense>()
        for (i in 0 until arr.length()) {
            val e = arr.getJSONObject(i)
            val catStr = e.optString("category", ExpenseCategory.OTHER.name)
            val methodStr = e.optString("paymentMethod", PaymentMethod.UPI.name)
            list.add(
                Expense(
                    id = e.optString("id", UUID.randomUUID().toString()),
                    libraryId = e.optString("libraryId", "lib_01"),
                    branchId = e.optString("branchId", "branch_01"),
                    category = try { ExpenseCategory.valueOf(catStr) } catch (err: Exception) { ExpenseCategory.OTHER },
                    title = e.optString("title", ""),
                    amount = e.optInt("amount", 0),
                    expenseDate = e.optString("expenseDate", ""),
                    paymentMethod = try { PaymentMethod.valueOf(methodStr) } catch (err: Exception) { PaymentMethod.UPI },
                    description = e.optString("description", "")
                )
            )
        }
        return list
    }

    private fun deserializeRequests(arr: JSONArray?): List<RegistrationRequest> {
        if (arr == null || arr.length() == 0) return emptyList()
        val list = mutableListOf<RegistrationRequest>()
        for (i in 0 until arr.length()) {
            val r = arr.getJSONObject(i)
            list.add(
                RegistrationRequest(
                    id = r.optString("id", UUID.randomUUID().toString()),
                    studentName = r.optString("studentName", ""),
                    mobile = r.optString("mobile", ""),
                    email = r.optString("email", ""),
                    course = r.optString("course", ""),
                    requestedShift = r.optString("requestedShift", ""),
                    preferredSeat = r.optString("preferredSeat", "Floating"),
                    requestDate = r.optString("requestDate", ""),
                    status = r.optString("status", "pending")
                )
            )
        }
        return list
    }

    private fun deserializeAuditLogs(arr: JSONArray?): List<AuditLog> {
        if (arr == null || arr.length() == 0) return emptyList()
        val list = mutableListOf<AuditLog>()
        for (i in 0 until arr.length()) {
            val l = arr.getJSONObject(i)
            list.add(
                AuditLog(
                    id = l.optString("id", UUID.randomUUID().toString()),
                    action = l.optString("action", ""),
                    entity = l.optString("entity", ""),
                    details = l.optString("details", ""),
                    timestamp = l.optString("timestamp", ""),
                    user = l.optString("user", "Admin")
                )
            )
        }
        return list
    }
}
