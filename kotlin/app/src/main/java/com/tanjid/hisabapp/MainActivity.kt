package com.tanjid.hisabapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.tanjid.hisabapp.ui.*
import com.tanjid.hisabapp.viewmodel.HisabViewModel

// 6 sections only — no QR Code, no Data Backup, nothing extra.
enum class Tab(val label: String, val icon: ImageVector) {
    Home("টালি", Icons.Filled.Home),
    Lenden("আয়-ব্যয়", Icons.Filled.AccountBalanceWallet),
    Daily("দৈনিক আয়", Icons.Filled.Work),
    Khata("বাকি খাতা", Icons.Filled.Book),
    Savings("সঞ্চয়", Icons.Filled.Savings),
    Report("রিপোর্ট", Icons.Filled.BarChart)
}

class MainActivity : ComponentActivity() {
    private val vm: HisabViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HisabTheme {
                val user by vm.currentUser.collectAsState()
                if (user == null) {
                    AuthScreen(vm)
                } else {
                    var tab by remember { mutableStateOf(Tab.Home) }
                    Scaffold(
                        bottomBar = {
                            NavigationBar(containerColor = HisabRed) {
                                Tab.values().forEach { t ->
                                    NavigationBarItem(
                                        selected = tab == t,
                                        onClick = { tab = t },
                                        icon = { Icon(t.icon, contentDescription = t.label) },
                                        label = { Text(t.label) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = HisabRed,
                                            unselectedIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                            selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                                            unselectedTextColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                            indicatorColor = MaterialTheme.colorScheme.surface
                                        )
                                    )
                                }
                            }
                        }
                    ) { padding ->
                        when (tab) {
                            Tab.Home -> HomeScreen(vm, Modifier.padding(padding))
                            Tab.Lenden -> TransactionScreen(vm, Modifier.padding(padding))
                            Tab.Daily -> DailyIncomeScreen(vm, Modifier.padding(padding))
                            Tab.Khata -> KhataScreen(vm, Modifier.padding(padding))
                            Tab.Savings -> SavingsScreen(vm, Modifier.padding(padding))
                            Tab.Report -> ReportScreen(vm, Modifier.padding(padding))
                        }
                    }
                }
            }
        }
    }
}
