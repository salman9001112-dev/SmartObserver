package com.smartobserver.traffic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CountItem(val id: Long, val type: String, val direction: String)

class TrafficViewModel : ViewModel() {
    private val _counts = MutableStateFlow<List<CountItem>>(emptyList())
    val counts = _counts.asStateFlow()

    fun addCount(type: String, dir: String) {
        val newItem = CountItem(System.currentTimeMillis(), type, dir)
        _counts.value = _counts.value + newItem
    }

    fun undo() {
        if (_counts.value.isNotEmpty()) {
            _counts.value = _counts.value.dropLast(1)
        }
    }

    fun reset() {
        _counts.value = emptyList()
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                var screen by remember { mutableStateOf("splash") }
                val vm: TrafficViewModel = viewModel()

                when (screen) {
                    "splash" -> SplashScreen(onStart = { screen = "survey" })
                    "survey" -> SurveyScreen(vm = vm, onFinish = { screen = "report" })
                    "report" -> ReportScreen(vm = vm, onBack = { screen = "splash" })
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onStart: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            Box(
                modifier = Modifier.size(200.dp).clip(CircleShape).background(Color(0xFF0052CC)).border(4.dp, Color(0xFF4C9AFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                    Row {
                        Icon(Icons.Default.Navigation, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                        Icon(Icons.Default.Traffic, contentDescription = null, tint = Color.Yellow, modifier = Modifier.size(32.dp))
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("الراصد الذكي", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("Smart Observer", fontSize = 18.sp, color = MaterialTheme.colorScheme.secondary)
            }

            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0052CC))
            ) {
                Text("إضافة رصد", fontSize = 22.sp, color = Color.White)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("منشئ التطبيق:", fontSize = 12.sp, color = Color.Gray)
                    Text("سلمان عبدالفتاح", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("دكتور المادة:", fontSize = 12.sp, color = Color.Gray)
                    Text("د. عبدالسلام الثور", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyScreen(vm: TrafficViewModel, onFinish: () -> Unit) {
    val counts by vm.counts.collectAsState()
    val types = listOf("سيارة", "شاحنة", "باص", "دراجة", "مشاة")
    val dirs = listOf("مستقيم ⬆️", "يمين ➡️", "يسار ⬅️", "يوتيرن ↩️")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("شاشة العد الميداني") },
                actions = {
                    IconButton(onClick = { vm.undo() }) { Icon(Icons.Default.Undo, contentDescription = null) }
                    IconButton(onClick = { vm.reset() }) { Icon(Icons.Default.Refresh, contentDescription = null) }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("الإجمالي: ${counts.size}", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Button(onClick = onFinish, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) {
                        Text("إنهاء الرصد")
                    }
                }
            }
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(padding).padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(types.flatMap { t -> dirs.map { d -> t to d } }) { (t, d) ->
                val c = counts.count { it.type == t && it.direction == d }
                Card(
                    onClick = { vm.addCount(t, d) },
                    modifier = Modifier.height(100.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.SpaceBetween) {
                        Text("$t - $d", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("$c", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(vm: TrafficViewModel, onBack: () -> Unit) {
    val counts by vm.counts.collectAsState()
    val total = counts.size
    val los = if (total < 50) "A (تدفق حر)" else if (total < 150) "C (مقبول)" else "E (مزدحم)"

    Scaffold(topBar = { TopAppBar(title = { Text("التقرير المروري (HCM)") }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("مستوى الخدمة (LOS)", fontSize = 14.sp)
                    Text(los, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D47A1))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("إجمالي المركبات المسجلة: $total", fontSize = 18.sp)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("العودة للرئيسية")
            }
        }
    }
}
