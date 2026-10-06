package com.arc.training

import android.graphics.Canvas
import android.graphics.Movie
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import java.util.UUID
import kotlin.math.max

private val BG = Color(0xFF07080A)
private val PANEL = Color(0xFF111419)
private val PANEL2 = Color(0xFF191D24)
private val TEXT = Color(0xFFF1F3F5)
private val MUTED = Color(0xFF9299A5)
private val ACCENT = Color(0xFFD7FF4B)
private val RED = Color(0xFFFF6262)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { ARC() }
    }
}

@Composable
private fun ARC() {
    val context = LocalContext.current
    val store = remember { AppStateStore(context) }
    val state = remember { mutableStateOf(store.load()) }
    val exercises = remember {
        mutableListOf<Exercise>().also {
            it.addAll(seedExercises())
            it.addAll(state.value.customExercises)
        }
    }
    var screen by remember { mutableStateOf("home") }

  val save: () -> Unit = { store.save(state.value) }
  val go: (String) -> Unit = { screen = it }
    fval refreshExercises: () -> Unit = {
    exercises.clear()
    exercises.addAll(seedExercises())
    exercises.addAll(state.value.customExercises)
}
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = BG,
            surface = PANEL,
            primary = ACCENT,
            onPrimary = Color.Black,
            onBackground = TEXT,
            onSurface = TEXT
        )
    ) {
        Surface(Modifier.fillMaxSize(), color = BG) {
            Scaffold(
                containerColor = BG,
                bottomBar = { BottomNav(screen) { go(it) } }
            ) { padding ->
                Box(Modifier.padding(padding)) {
                    when {
                        screen == "home" -> Home(state.value, exercises) { target -> go(target) }
                        screen == "exercises" -> Library(state.value, exercises, save, refreshExercises) { id -> go("exercise:$id") }
                        screen == "plans" -> Plans(state.value, save) { id -> go("plan:$id") }
                        screen == "workout" -> Workout(state.value, exercises, save) { target -> go(target) }
                        screen == "more" -> More { target -> go(target) }
                        screen == "goals" -> Goals(state.value, save) { go("more") }
                        screen == "history" -> History(state.value, exercises) { go("more") }
                        screen == "checklist" -> Checklist(state.value, save) { go("more") }
                        screen == "settings" -> Settings(state.value, save) { go("more") }
                        screen.startsWith("exercise:") -> {
                            val id = screen.removePrefix("exercise:")
                            exercises.firstOrNull { it.id == id }?.let { ExerciseDetail(it, state.value, save) { go("exercises") } }
                        }
                        screen.startsWith("plan:") -> {
                            val id = screen.removePrefix("plan:")
                            state.value.plans.firstOrNull { it.id == id }?.let { PlanEditor(it, state.value, exercises, save) { go("plans") } }
                        }
                        screen.startsWith("warmup:") -> Warmup(screen.removePrefix("warmup:")) { go("plans") }
                        screen.startsWith("stretch:") -> Stretch(screen.removePrefix("stretch:")) { go("workout") }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomNav(current: String, nav: (String) -> Unit) {
    val items = listOf(
        "home" to Icons.Default.Home,
        "exercises" to Icons.Default.FitnessCenter,
        "plans" to Icons.Default.CalendarMonth,
        "workout" to Icons.Default.PlayArrow,
        "more" to Icons.Default.MoreHoriz
    )
    NavigationBar(containerColor = Color(0xFF0C0E12)) {
        items.forEach { (route, icon) ->
            NavigationBarItem(
                selected = current == route || (current.contains(":") && route == "more"),
                onClick = { nav(route) },
                icon = { Icon(icon, null) },
                label = { Text(route.replaceFirstChar { it.uppercase() }) }
            )
        }
    }
}

@Composable
private fun Header(title: String, subtitle: String = "", back: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
        if (back != null) {
            IconButton(back) { Icon(Icons.Default.ArrowBack, null) }
            Spacer(Modifier.width(2.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 27.sp, fontWeight = FontWeight.Black, letterSpacing = 0.8.sp)
            if (subtitle.isNotBlank()) Text(subtitle, color = MUTED, fontSize = 12.sp)
        }
        Text("ARC", color = ACCENT, fontWeight = FontWeight.Black, letterSpacing = 1.8.sp)
    }
}

@Composable
private fun CardBox(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = PANEL)) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun Metric(label: String, value: String, modifier: Modifier) {
    CardBox(modifier) {
        Text(label, color = MUTED, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(3.dp))
        Text(value, fontSize = 23.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun Home(state: AppState, exercises: List<Exercise>, go: (String) -> Unit) {
    val plan = state.plans.firstOrNull { it.id == state.selectedPlanId } ?: state.plans.firstOrNull()
    val day = plan?.days?.firstOrNull { it.id == state.selectedDayId } ?: plan?.days?.firstOrNull()
    LazyColumn(contentPadding = PaddingValues(bottom = 110.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Header("ARC", "Train. Track. Evolve.") }
        item {
            CardBox(Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                Image(
                    painter = painterResource(com.arc.training.R.drawable.arc_thumbnail),
                    contentDescription = "ARC",
                    modifier = Modifier.fillMaxWidth().height(205.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(12.dp))
                Text("TODAY'S WORKOUT", color = MUTED, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(day?.name ?: "Build your first split", fontSize = 25.sp, fontWeight = FontWeight.Black)
                Text(
                    day?.let { "${it.exercises.size} exercises • ${it.exercises.sumOf { p -> p.sets }} work sets" } ?: "Create a plan, then choose today's day.",
                    color = MUTED
                )
                Spacer(Modifier.height(11.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { go(if (day == null) "plans" else "warmup:${primaryMuscle(day, exercises)}") },
                        Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ACCENT, contentColor = Color.Black)
                    ) { Text("WARM-UP") }
                    OutlinedButton({ go("workout") }, Modifier.weight(1f)) { Text("WORKOUT") }
                }
            }
        }
        item {
            Row(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Metric("SESSIONS", state.sessions.size.toString(), Modifier.weight(1f))
                Metric("STREAK", state.currentStreak.toString(), Modifier.weight(1f))
                Metric("GOALS", state.goals.count { it.active }.toString(), Modifier.weight(1f))
            }
        }
        item {
            CardBox(Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                Text("QUICK ACTIONS", fontWeight = FontWeight.Bold)
                QuickAction("Customize workouts", Icons.Default.Tune) { go("plans") }
                QuickAction("Exercise library", Icons.Default.FitnessCenter) { go("exercises") }
                QuickAction("Full-body warm-up", Icons.Default.PlayArrow) { go("warmup:Full Body") }
                QuickAction("Goals", Icons.Default.Flag) { go("goals") }
                QuickAction("Checklist", Icons.Default.Check) { go("checklist") }
            }
        }
    }
}

@Composable
private fun QuickAction(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = ACCENT)
        Spacer(Modifier.width(9.dp))
        Text(title, Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
        Text("›", color = MUTED, fontSize = 24.sp)
    }
}

@Composable
private fun Library(state: AppState, exercises: MutableList<Exercise>, save: () -> Unit, refresh: () -> Unit, open: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    var muscle by remember { mutableStateOf("All") }
    var equipment by remember { mutableStateOf("All") }
    var favorites by remember { mutableStateOf(false) }
    var showCustom by remember { mutableStateOf(false) }
    val muscles = listOf("All","Chest","Back","Lats","Shoulders","Biceps","Triceps","Quadriceps","Hamstrings","Glutes","Calves","Abdominals","Obliques","Forearms")
    val equipmentList = listOf("All","Barbell","Dumbbell","Cable","Machine","Bodyweight","Smith Machine","Kettlebell")
    val filtered = exercises.filter { e ->
        val q = query.isBlank() || e.name.contains(query, true) || e.primaryMuscle.contains(query, true) || e.equipment.contains(query, true)
        val m = muscle == "All" || e.primaryMuscle.contains(muscle, ignoreCase = true) || e.secondaryMuscles.joinToString("|").contains(muscle, ignoreCase = true)
        val eq = equipment == "All" || e.equipment.equals(equipment, true)
        val f = !favorites || state.favorites.contains(e.id)
        q && m && eq && f
    }
    LazyColumn(contentPadding = PaddingValues(bottom = 110.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Header("EXERCISES", "Organized by muscle, equipment and movement") }
        item {
            OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth().padding(horizontal = 16.dp), singleLine = true, label = { Text("Search exercises") })
        }
        item { ChipRow(muscles, muscle) { muscle = it } }
        item { ChipRow(equipmentList, equipment) { equipment = it } }
        item {
            Row(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                FilterChip(favorites, { favorites = !favorites }, label = { Text("Favorites") })
                Surface(color = PANEL2, shape = RoundedCornerShape(99.dp)) { Text("${filtered.size} shown", Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = MUTED, fontSize = 11.sp) }
            }
        }
        item {
            Button(onClick = { showCustom = true }, Modifier.padding(horizontal = 16.dp).fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = ACCENT, contentColor = Color.Black)) {
                Icon(Icons.Default.Add, null); Spacer(Modifier.width(5.dp)); Text("CREATE CUSTOM EXERCISE")
            }
        }
        items(filtered, key = { it.id }) { e ->
            CardBox(Modifier.padding(horizontal = 16.dp).fillMaxWidth().clickable { open(e.id) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ExerciseThumb(e, Modifier.size(78.dp))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(e.name, fontWeight = FontWeight.Bold)
                        Text(e.primaryMuscle, color = ACCENT, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("${e.equipment} • ${e.difficulty}", color = MUTED, fontSize = 11.sp)
                        Text(patternLabel(e.pattern), color = MUTED, fontSize = 11.sp)
                    }
                    IconButton({ if (state.favorites.contains(e.id)) state.favorites.remove(e.id) else state.favorites.add(e.id); save() }) {
                        Icon(if (state.favorites.contains(e.id)) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null, tint = ACCENT)
                    }
                }
            }
        }
    }
    if (showCustom) CustomExerciseDialog({ showCustom = false }, { name, muscleName, eq, pattern ->
        state.customExercises.add(Exercise(name = name, primaryMuscle = muscleName, equipment = eq, pattern = pattern, isCustom = true))
        save(); refresh(); showCustom = false
    })
}

@Composable
private fun ChipRow(values: List<String>, selected: String, choose: (String) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        values.forEach { value -> FilterChip(value == selected, { choose(value) }, label = { Text(value) }) }
    }
}

@Composable
private fun ExerciseThumb(ex: Exercise, modifier: Modifier) {
    GifPlayer(demoRes(ex.demo), 1f, modifier)
}

@Composable
private fun ExerciseDetail(ex: Exercise, state: AppState, save: () -> Unit, back: () -> Unit) {
    var speed by remember { mutableStateOf(1f) }
    var showQuick by remember { mutableStateOf(false) }
    LazyColumn(contentPadding = PaddingValues(bottom = 110.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        item { Header(ex.name.uppercase(), "${ex.primaryMuscle} • ${ex.equipment}", back) }
        item {
            CardBox(Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                GifPlayer(demoRes(ex.demo), speed, Modifier.fillMaxWidth().height(300.dp))
                Spacer(Modifier.height(9.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Tag(ex.primaryMuscle); Tag(ex.equipment); Tag(ex.difficulty); Tag(patternLabel(ex.pattern))
                }
                Spacer(Modifier.height(8.dp))
                Text("DEMONSTRATION SPEED", color = MUTED, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Slider(value = speed, onValueChange = { speed = it }, valueRange = 0.5f..1.5f)
            }
        }
        item { InfoCard("PRIMARY MUSCLE", ex.primaryMuscle + if (ex.secondaryMuscles.isEmpty()) "" else "\nSecondary: ${ex.secondaryMuscles.joinToString()}") }
        val guide = guideFor(ex)
        item { InfoCard("HOW TO PERFORM", guide.setup) }
        item { InfoCard("START POSITION", guide.start) }
        item { InfoCard("STEP-BY-STEP", guide.steps) }
        item { InfoCard("BREATHING", guide.breathing) }
        item { InfoCard("TEMPO / CONTROL", guide.tempo) }
        item { InfoCard("RANGE OF MOTION", guide.rom) }
        item { InfoCard("COMMON MISTAKES", guide.mistakes) }
        item { InfoCard("BEGINNER NOTES", guide.beginner) }
        item {
            Row(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button({ addExerciseToPlan(ex, state); save() }, Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = ACCENT, contentColor = Color.Black)) { Text("ADD TO PLAN") }
                OutlinedButton({ showQuick = true }, Modifier.weight(1f)) { Text("QUICK FORM") }
            }
        }
    }
    if (showQuick) AlertDialog(onDismissRequest = { showQuick = false }, title = { Text("Quick form") }, text = { Text("Stable setup • controlled path • no unnecessary momentum • comfortable range • controlled return.") }, confirmButton = { TextButton({ showQuick = false }) { Text("DONE") } })
}

@Composable
private fun InfoCard(title: String, text: String) {
    CardBox(Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
        Text(title, color = ACCENT, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 1.1.sp)
        Spacer(Modifier.height(6.dp))
        Text(text, lineHeight = 20.sp)
    }
}

@Composable
private fun Tag(text: String) {
    Surface(color = PANEL2, shape = RoundedCornerShape(99.dp)) { Text(text, Modifier.padding(horizontal = 8.dp, vertical = 5.dp), fontSize = 10.sp) }
}

private fun addExerciseToPlan(ex: Exercise, state: AppState) {
    val plan = state.plans.firstOrNull() ?: WorkoutPlan(name = "My Plan").also {
        it.days.add(WorkoutDay(name = "Day 1")); state.plans.add(it); state.selectedPlanId = it.id; state.selectedDayId = it.days.first().id
    }
    val day = plan.days.firstOrNull { it.id == state.selectedDayId } ?: plan.days.first()
    if (day.exercises.none { it.exerciseId == ex.id }) day.exercises.add(PlannedExercise(ex.id))
}

@Composable
private fun Plans(state: AppState, save: () -> Unit, open: (String) -> Unit) {
    var newPlan by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    LazyColumn(contentPadding = PaddingValues(bottom = 110.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        item { Header("WORKOUT BUILDER", "Unlimited plans • days • exercises • full configuration") }
        item {
            Button({ newPlan = true }, Modifier.padding(horizontal = 16.dp).fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = ACCENT, contentColor = Color.Black)) { Text("CREATE NEW PLAN") }
        }
        items(state.plans, key = { it.id }) { plan ->
            CardBox(Modifier.padding(horizontal = 16.dp).fillMaxWidth().clickable { open(plan.id) }) {
                Text(plan.name, fontWeight = FontWeight.Black, fontSize = 19.sp)
                Text("${plan.days.size} days • ${plan.days.count { !it.restDay }} training days", color = MUTED)
                Text("Tap to customize every day and exercise.", color = MUTED, fontSize = 12.sp)
            }
        }
        if (state.plans.isEmpty()) item { Text("No plans yet.", color = MUTED, modifier = Modifier.padding(16.dp)) }
    }
    if (newPlan) AlertDialog(
        onDismissRequest = { newPlan = false },
        title = { Text("Create plan") },
        text = { OutlinedTextField(name, { name = it }, label = { Text("Plan name") }, singleLine = true) },
        confirmButton = { Button({
            if (name.isNotBlank()) {
                val plan = WorkoutPlan(name = name.trim()); plan.days.add(WorkoutDay(name = "Day 1")); state.plans.add(plan); state.selectedPlanId = plan.id; state.selectedDayId = plan.days.first().id; save(); name = ""; newPlan = false
            }
        }, colors = ButtonDefaults.buttonColors(containerColor = ACCENT, contentColor = Color.Black)) { Text("CREATE") } },
        dismissButton = { TextButton({ newPlan = false }) { Text("CANCEL") } }
    )
}

@Composable
private fun PlanEditor(plan: WorkoutPlan, state: AppState, exercises: List<Exercise>, save: () -> Unit, back: () -> Unit) {
    var selectedId by remember(plan.id) { mutableStateOf(plan.days.firstOrNull()?.id) }
    var addDay by remember { mutableStateOf(false) }
    var addExercise by remember { mutableStateOf(false) }
    var editExercise by remember { mutableStateOf<PlannedExercise?>(null) }
    var editDay by remember { mutableStateOf<WorkoutDay?>(null) }
    val day = plan.days.firstOrNull { it.id == selectedId } ?: plan.days.firstOrNull()

    LazyColumn(contentPadding = PaddingValues(bottom = 110.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        item { Header(plan.name.uppercase(), "Customize everything", back) }
        item { Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) { plan.days.forEach { d -> FilterChip(d.id == day?.id, { selectedId = d.id }, label = { Text(d.name) }) } } }
        item {
            Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Button({ addDay = true }, Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = ACCENT, contentColor = Color.Black)) { Text("ADD DAY") }
                OutlinedButton({ plan.days.add(WorkoutDay(name = "Rest", restDay = true)); selectedId = plan.days.last().id; save() }, Modifier.weight(1f)) { Text("REST DAY") }
            }
        }
        day?.let { d ->
            item {
                CardBox(Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(d.name, fontSize = 21.sp, fontWeight = FontWeight.Black)
                            Text(if (d.restDay) "Rest" else "${d.exercises.size} exercises • ${d.exercises.sumOf { it.sets }} sets", color = MUTED)
                        }
                        IconButton(onClick = { editDay = d }) { Icon(Icons.Default.Edit, null) }
                    }
                    if (!d.restDay) Button({ addExercise = true }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = ACCENT, contentColor = Color.Black)) { Text("ADD EXERCISE") }
                }
            }
            itemsIndexed(d.exercises) { index, p ->
                val ex = exercises.firstOrNull { it.id == p.exerciseId }
                if (ex != null) CardBox(Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ExerciseThumb(ex, Modifier.size(58.dp))
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text("${index + 1}. ${ex.name}", fontWeight = FontWeight.Bold)
                            Text("${p.sets} × ${p.repsMin}-${p.repsMax} • ${p.weight.clean()} ${state.weightUnit} • ${p.restSeconds}s", color = MUTED, fontSize = 12.sp)
                            Text("RIR ${p.rir} • RPE ${p.rpe.clean()} • tempo ${p.tempo} • warm-ups ${p.warmupSets}", color = MUTED, fontSize = 11.sp)
                        }
                        IconButton({ editExercise = p }) { Icon(Icons.Default.Edit, null) }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        IconButton(enabled = index > 0, onClick = { java.util.Collections.swap(d.exercises, index, index - 1); save() }) { Icon(Icons.Default.ArrowUpward, null) }
                        IconButton(enabled = index < d.exercises.lastIndex, onClick = { java.util.Collections.swap(d.exercises, index, index + 1); save() }) { Icon(Icons.Default.ArrowDownward, null) }
                        IconButton({ d.exercises.removeAt(index); save() }) { Icon(Icons.Default.Delete, null, tint = RED) }
                    }
                }
            }
            item {
                Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    OutlinedButton({ state.selectedPlanId = plan.id; state.selectedDayId = d.id; save() }, Modifier.weight(1f)) { Text("SET AS TODAY") }
                    OutlinedButton({ if (plan.days.size > 1) { plan.days.removeAll { it.id == d.id }; selectedId = plan.days.firstOrNull()?.id; save() } }, Modifier.weight(1f)) { Text("DELETE DAY") }
                }
            }
        }
    }
    if (addDay) DayDialog("Add day", "", { text -> if (text.isNotBlank()) { plan.days.add(WorkoutDay(name = text.trim())); selectedId = plan.days.last().id; save() }; addDay = false }, { addDay = false })
    if (editDay != null) {
        val current = editDay!!
        DayDialog("Rename day", current.name, { text -> if (text.isNotBlank()) current.name = text.trim(); save(); editDay = null }, { editDay = null })
    }
    if (addExercise && day != null) AddExerciseDialog(exercises, day) { addExercise = false; save() }
    if (editExercise != null) ConfigExerciseDialog(editExercise!!, state.weightUnit, save) { editExercise = null }
}


@Composable
private fun DayDialog(title: String, start: String, done: (String) -> Unit, cancel: () -> Unit = {}) {
    var text by remember(start) { mutableStateOf(start) }
    AlertDialog(
        onDismissRequest = cancel,
        title = { Text(title) },
        text = { OutlinedTextField(text, { text = it }, label = { Text("Name") }, singleLine = true) },
        confirmButton = { Button({ done(text) }, colors = ButtonDefaults.buttonColors(containerColor = ACCENT, contentColor = Color.Black)) { Text("SAVE") } },
        dismissButton = { TextButton(cancel) { Text("CANCEL") } }
    )
}

@Composable
private fun AddExerciseDialog(exercises: List<Exercise>, day: WorkoutDay, close: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val list = exercises.filter { query.isBlank() || it.name.contains(query, true) || it.primaryMuscle.contains(query, true) }.take(120)
    AlertDialog(onDismissRequest = close, title = { Text("Choose exercise") }, text = {
        Column {
            OutlinedTextField(query, { query = it }, label = { Text("Search") }, singleLine = true)
            Spacer(Modifier.height(8.dp))
            LazyColumn(Modifier.height(420.dp)) {
                items(list, key = { it.id }) { ex ->
                    Row(Modifier.fillMaxWidth().clickable { if (day.exercises.none { it.exerciseId == ex.id }) day.exercises.add(PlannedExercise(ex.id)); close() }.padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                        ExerciseThumb(ex, Modifier.size(52.dp)); Spacer(Modifier.width(8.dp)); Column(Modifier.weight(1f)) { Text(ex.name, fontWeight = FontWeight.SemiBold); Text("${ex.primaryMuscle} • ${ex.equipment}", color = MUTED, fontSize = 11.sp) }; Icon(Icons.Default.Add, null, tint = ACCENT)
                    }
                }
            }
        }
    }, confirmButton = { TextButton(close) { Text("DONE") } })
}

@Composable
private fun ConfigExerciseDialog(p: PlannedExercise, unit: String, save: () -> Unit, close: () -> Unit) {
    var sets by remember { mutableStateOf(p.sets.toString()) }; var min by remember { mutableStateOf(p.repsMin.toString()) }; var maxR by remember { mutableStateOf(p.repsMax.toString()) }; var weight by remember { mutableStateOf(p.weight.toString()) }; var rest by remember { mutableStateOf(p.restSeconds.toString()) }; var rir by remember { mutableStateOf(p.rir.toString()) }; var rpe by remember { mutableStateOf(p.rpe.toString()) }; var tempo by remember { mutableStateOf(p.tempo) }; var warm by remember { mutableStateOf(p.warmupSets.toString()) }; var notes by remember { mutableStateOf(p.notes) }
    AlertDialog(onDismissRequest = close, title = { Text("Configure exercise") }, text = {
        LazyColumn(Modifier.height(540.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            item { Num("Sets", sets) { sets = it } }; item { Num("Minimum reps", min) { min = it } }; item { Num("Maximum reps", maxR) { maxR = it } }; item { Num("Target weight ($unit)", weight) { weight = it } }; item { Num("Rest seconds", rest) { rest = it } }; item { Num("RIR", rir) { rir = it } }; item { Num("RPE", rpe) { rpe = it } }; item { Num("Warm-up sets", warm) { warm = it } }; item { OutlinedTextField(tempo, { tempo = it }, label = { Text("Tempo e.g. 2-1-2") }, singleLine = true) }; item { OutlinedTextField(notes, { notes = it }, label = { Text("Notes / cues") }, minLines = 3) }
        }
    }, confirmButton = { Button({ p.sets = sets.toIntOrNull()?.coerceIn(1, 30) ?: p.sets; p.repsMin = min.toIntOrNull()?.coerceIn(1, 100) ?: p.repsMin; p.repsMax = maxR.toIntOrNull()?.coerceIn(p.repsMin, 100) ?: p.repsMax; p.weight = weight.toDoubleOrNull()?.coerceAtLeast(0.0) ?: p.weight; p.restSeconds = rest.toIntOrNull()?.coerceIn(15, 900) ?: p.restSeconds; p.rir = rir.toIntOrNull()?.coerceIn(0, 5) ?: p.rir; p.rpe = rpe.toDoubleOrNull()?.coerceIn(1.0, 10.0) ?: p.rpe; p.tempo = tempo.ifBlank { "2-1-2" }; p.warmupSets = warm.toIntOrNull()?.coerceIn(0, 8) ?: p.warmupSets; p.notes = notes; save(); close() }, colors = ButtonDefaults.buttonColors(containerColor = ACCENT, contentColor = Color.Black)) { Text("SAVE") } }, dismissButton = { TextButton(close) { Text("CANCEL") } })
}

@Composable
private fun Num(label: String, value: String, change: (String) -> Unit) = OutlinedTextField(value, change, label = { Text(label) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())

@Composable
private fun Workout(state: AppState, exercises: List<Exercise>, save: () -> Unit, go: (String) -> Unit) {
    val plan = state.plans.firstOrNull { it.id == state.selectedPlanId } ?: state.plans.firstOrNull(); val day = plan?.days?.firstOrNull { it.id == state.selectedDayId } ?: plan?.days?.firstOrNull()
    var active by remember { mutableStateOf(false) }; var exIndex by remember { mutableIntStateOf(0) }; var setIndex by remember { mutableIntStateOf(0) }; var weight by remember { mutableStateOf("") }; var reps by remember { mutableStateOf("") }; var rir by remember { mutableStateOf("2") }; var rest by remember { mutableIntStateOf(0) }; var timer by remember { mutableStateOf(false) }; var session by remember { mutableStateOf<WorkoutSession?>(null) }
    LaunchedEffect(timer) { while (timer && rest > 0) { delay(1000); rest-- }; if (rest <= 0) timer = false }

    if (plan == null || day == null || day.restDay || day.exercises.isEmpty()) {
        Column { Header("WORKOUT", "Nothing selected"); CardBox(Modifier.padding(16.dp).fillMaxWidth()) { Text("Build a workout first", fontSize = 22.sp, fontWeight = FontWeight.Black); Text("Create a plan, configure a day and set it as today.", color = MUTED); Spacer(Modifier.height(10.dp)); Button({ go("plans") }, colors = ButtonDefaults.buttonColors(containerColor = ACCENT, contentColor = Color.Black)) { Text("OPEN WORKOUT BUILDER") } } }
        return
    }
    if (!active) {
        LazyColumn(contentPadding = PaddingValues(bottom = 110.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            item { Header("WORKOUT", day.name) }
            item { CardBox(Modifier.padding(16.dp).fillMaxWidth()) {
                Text("${day.exercises.size} exercises", fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text("${day.exercises.sumOf { it.sets }} work sets • warm-up sets are configurable per exercise", color = MUTED)
                Spacer(Modifier.height(8.dp))
                day.exercises.forEach { p -> exercises.firstOrNull { it.id == p.exerciseId }?.let { Text("${it.name} — ${p.sets} × ${p.repsMin}-${p.repsMax}", color = MUTED, fontSize = 12.sp) } }
                Spacer(Modifier.height(10.dp))
                Button({ state.selectedPlanId = plan.id; state.selectedDayId = day.id; session = WorkoutSession(planId = plan.id, dayId = day.id, startedAt = System.currentTimeMillis()); exIndex = 0; setIndex = 0; active = true; save() }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = ACCENT, contentColor = Color.Black)) { Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(5.dp)); Text("START WORKOUT") }
                OutlinedButton({ go("warmup:${primaryMuscle(day, exercises)}") }, Modifier.fillMaxWidth()) { Text("OPEN WARM-UP") }
            } }
        }
        return
    }
    val p = day.exercises.getOrNull(exIndex) ?: return; val ex = exercises.firstOrNull { it.id == p.exerciseId } ?: return
    LazyColumn(contentPadding = PaddingValues(bottom = 110.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        item { Header("SET ${setIndex + 1} / ${p.sets}", ex.name, back = { active = false; timer = false }) }
        item { CardBox(Modifier.padding(horizontal = 16.dp).fillMaxWidth()) { ExerciseHero(ex); Spacer(Modifier.height(8.dp)); Text("TARGET", color = MUTED, fontSize = 10.sp, fontWeight = FontWeight.Bold); Text("${p.repsMin}-${p.repsMax} reps • ${p.weight.clean()} ${state.weightUnit} • RIR ${p.rir}", fontWeight = FontWeight.Bold); if (p.warmupSets > 0) Text("Warm-up sets: ${p.warmupSets}", color = ACCENT, fontSize = 12.sp) } }
        item { CardBox(Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { Num("Weight", weight) { weight = it }; Num("Reps", reps) { reps = it }; Num("RIR", rir) { rir = it } }
            Spacer(Modifier.height(9.dp))
            Button({ val w = weight.toDoubleOrNull() ?: p.weight; val r = reps.toIntOrNull() ?: p.repsMin; val rr = rir.toIntOrNull() ?: p.rir; session?.sets?.add(LoggedSet(ex.id, w, r, rr)); rest = p.restSeconds; timer = true; weight = w.toString(); reps = ""; save(); if (setIndex + 1 < p.sets) setIndex++ else if (exIndex + 1 < day.exercises.size) { exIndex++; setIndex = 0 } else { session?.copy(endedAt = System.currentTimeMillis())?.let { state.sessions.add(it) }; state.currentStreak++; save(); active = false; go("stretch:${primaryMuscle(day, exercises)}") } }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = ACCENT, contentColor = Color.Black)) { Text("SAVE SET") }
            if (rest > 0) { Spacer(Modifier.height(8.dp)); Text("REST $rest s", fontSize = 32.sp, fontWeight = FontWeight.Black); Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { OutlinedButton({ rest += 15 }) { Text("+15") }; OutlinedButton({ rest = max(0, rest - 15) }) { Text("-15") }; OutlinedButton({ rest = 0; timer = false }) { Text("SKIP") } } }
        } }
        item { CardBox(Modifier.padding(horizontal = 16.dp).fillMaxWidth()) { Text("PROGRAMMED", fontWeight = FontWeight.Bold); Text("Tempo ${p.tempo} • Rest ${p.restSeconds}s • Warm-ups ${p.warmupSets}", color = MUTED, fontSize = 12.sp); if (p.notes.isNotBlank()) Text(p.notes, color = MUTED, fontSize = 12.sp) } }
    }
}

@Composable
private fun ExerciseHero(ex: Exercise) = GifPlayer(demoRes(ex.demo), 1f, Modifier.fillMaxWidth().height(300.dp))

@Composable
private fun GifPlayer(resId: Int, speed: Float, modifier: Modifier) {
    AndroidView(
        modifier = modifier.clip(RoundedCornerShape(18.dp)).background(Color(0xFF0D0F13)),
        factory = { context -> LoopingGifView(context, resId) },
        update = { it.speed = speed }
    )
}

private class LoopingGifView(private val context: android.content.Context, private val resId: Int) : View(context) {
    private val movie: Movie? = context.resources.openRawResource(resId).use { Movie.decodeStream(it) }
    var speed: Float = 1f
    private val started = SystemClock.uptimeMillis()
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val m = movie ?: return
        val duration = max(1, m.duration())
        val elapsed = ((SystemClock.uptimeMillis() - started) * speed).toInt() % duration
        m.setTime(elapsed)
        val scale = minOf(width.toFloat() / m.width().coerceAtLeast(1), height.toFloat() / m.height().coerceAtLeast(1))
        val left = (width - m.width() * scale) / 2f; val top = (height - m.height() * scale) / 2f
        canvas.save(); canvas.scale(scale, scale); canvas.translate(left / scale, top / scale); m.draw(canvas, 0f, 0f); canvas.restore()
        postInvalidateDelayed(33L)
    }
}

@Composable
private fun Warmup(muscle: String, back: () -> Unit) {
    val steps = warmups[muscle] ?: warmups["Full Body"]!!
    var index by remember(muscle) { mutableIntStateOf(0) }; var seconds by remember(muscle) { mutableIntStateOf(steps[0].seconds) }; var running by remember { mutableStateOf(false) }
    LaunchedEffect(index) { seconds = steps[index].seconds; running = false }; LaunchedEffect(running) { while (running && seconds > 0) { delay(1000); seconds-- }; if (seconds <= 0) running = false }
    LazyColumn(contentPadding = PaddingValues(bottom = 110.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        item { Header("WARM-UP", muscle, back) }
        item { CardBox(Modifier.padding(horizontal = 16.dp).fillMaxWidth()) { Text("ADAPTIVE WARM-UP", color = ACCENT, fontWeight = FontWeight.Black); Text("General heat → joints → muscle prep → movement rehearsal", color = MUTED) } }
        itemsIndexed(steps) { i, s -> CardBox(Modifier.padding(horizontal = 16.dp).fillMaxWidth()) { Text("${i + 1}. ${s.title}", fontWeight = FontWeight.Bold, fontSize = 17.sp); Text(s.instructions, color = MUTED, lineHeight = 19.sp); if (i == index) { Spacer(Modifier.height(7.dp)); Text("${seconds}s", fontSize = 28.sp, fontWeight = FontWeight.Black); Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { Button({ running = !running }, colors = ButtonDefaults.buttonColors(containerColor = ACCENT, contentColor = Color.Black)) { Text(if (running) "PAUSE" else "START") }; OutlinedButton({ seconds = s.seconds; running = false }) { Icon(Icons.Default.RestartAlt, null); Text("RESET") }; OutlinedButton({ if (index < steps.lastIndex) index++ }) { Icon(Icons.Default.SkipNext, null); Text("NEXT") } } } } }
    }
}

@Composable
private fun Stretch(muscle: String, back: () -> Unit) {
    val steps = stretches[muscle] ?: stretches["Full Body"]!!
    LazyColumn(contentPadding = PaddingValues(bottom = 110.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        item { Header("COOL-DOWN", muscle, back) }
        items(steps) { s -> CardBox(Modifier.padding(horizontal = 16.dp).fillMaxWidth()) { Text(s.title, fontWeight = FontWeight.Bold, fontSize = 17.sp); Text(s.target, color = ACCENT, fontSize = 11.sp); Text(s.instructions, color = MUTED, lineHeight = 19.sp) } }
    }
}

@Composable
private fun More(go: (String) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(bottom = 110.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        item { Header("MORE", "Progress and utilities") }
        item { CardBox(Modifier.padding(horizontal = 16.dp).fillMaxWidth()) { QuickAction("Goals & progress", Icons.Default.Flag) { go("goals") }; QuickAction("History & PRs", Icons.Default.History) { go("history") }; QuickAction("Checklist", Icons.Default.Check) { go("checklist") }; QuickAction("Settings", Icons.Default.Settings) { go("settings") } } }
    }
}

@Composable
private fun Goals(state: AppState, save: () -> Unit, back: () -> Unit) {
    var show by remember { mutableStateOf(false) }; var name by remember { mutableStateOf("") }; var target by remember { mutableStateOf("") }
    LazyColumn(contentPadding = PaddingValues(bottom = 110.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Header("GOALS", "Strength • reps • frequency • consistency", back) }
        item { Button({ show = true }, Modifier.padding(horizontal = 16.dp).fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = ACCENT, contentColor = Color.Black)) { Text("CREATE GOAL") } }
        items(state.goals, key = { it.id }) { g -> val p = if (g.target > 0) (g.current / g.target).coerceIn(0.0, 1.0) else 0.0; CardBox(Modifier.padding(horizontal = 16.dp).fillMaxWidth()) { Text(g.name, fontWeight = FontWeight.Bold); Text("${g.current.clean()} / ${g.target.clean()}", color = MUTED); LinearProgressIndicator(progress = p.toFloat(), modifier = Modifier.fillMaxWidth(), color = ACCENT) } }
    }
    if (show) AlertDialog(onDismissRequest={show=false}, title={Text("Create goal")}, text={Column(verticalArrangement=Arrangement.spacedBy(7.dp)){OutlinedTextField(name,{name=it},label={Text("Goal name")});OutlinedTextField(target,{target=it},label={Text("Target")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal))}}, confirmButton={Button({if(name.isNotBlank()){state.goals.add(Goal(name=name.trim(),target=target.toDoubleOrNull()?:1.0));save();name="";target="";show=false}}, colors=ButtonDefaults.buttonColors(containerColor=ACCENT,contentColor=Color.Black)){Text("CREATE")}}, dismissButton={TextButton({show=false}){Text("CANCEL")}})
}

@Composable
private fun Checklist(state: AppState, save: () -> Unit, back: () -> Unit) {
    if (state.checklists.isEmpty()) { state.checklists.add(Checklist(name = "Daily")); save() }
    var selected by remember { mutableStateOf(state.checklists.first().id) }; var show by remember { mutableStateOf(false) }; var text by remember { mutableStateOf("") }
    val list = state.checklists.firstOrNull { it.id == selected } ?: state.checklists.first()
    Column {
        Header("CHECKLIST", "Daily • Gym • Study • Business • Personal", back)
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) { state.checklists.forEach { c -> FilterChip(c.id == list.id, { selected = c.id }, label = { Text(c.name) }) } }
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) { Button({show=true}, Modifier.weight(1f), colors=ButtonDefaults.buttonColors(containerColor=ACCENT,contentColor=Color.Black)){Text("ADD ITEM")}; OutlinedButton({val c=Checklist(name="Custom ${state.checklists.size+1}");state.checklists.add(c);selected=c.id;save()},Modifier.weight(1f)){Text("NEW LIST")} }
        LazyColumn(contentPadding=PaddingValues(16.dp,0.dp,16.dp,110.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){items(list.items,key={it.id}){i->CardBox(Modifier.fillMaxWidth()){Row(verticalAlignment=Alignment.CenterVertically){Checkbox(i.done,{i.done=it;save()});Text(i.title,Modifier.weight(1f));IconButton({list.items.remove(i);save()}){Icon(Icons.Default.Delete,null,tint=RED)}}}}}
    }
    if(show) AlertDialog(onDismissRequest={show=false},title={Text("Checklist item")},text={OutlinedTextField(text,{text=it},label={Text("Item")},singleLine=true)},confirmButton={Button({if(text.isNotBlank())list.items.add(ChecklistItem(title=text.trim()));save();text="";show=false},colors=ButtonDefaults.buttonColors(containerColor=ACCENT,contentColor=Color.Black)){Text("ADD")}},dismissButton={TextButton({show=false}){Text("CANCEL")}})
}

@Composable
private fun History(state: AppState, exercises: List<Exercise>, back: () -> Unit) {
    LazyColumn(contentPadding=PaddingValues(bottom=110.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){item{Header("HISTORY","Logged workouts and sets",back)};if(state.sessions.isEmpty())item{Text("No completed workouts yet.",color=MUTED,modifier=Modifier.padding(16.dp))};items(state.sessions.asReversed()){s->CardBox(Modifier.padding(horizontal=16.dp).fillMaxWidth()){val d=state.plans.flatMap{it.days}.firstOrNull{it.id==s.dayId};Text(d?.name?:"Workout",fontWeight=FontWeight.Bold);Text("${s.sets.size} logged sets",color=MUTED);s.sets.take(10).forEach{l->val e=exercises.firstOrNull{it.id==l.exerciseId};Text("${e?.name?:("Exercise")} — ${l.weight.clean()} kg × ${l.reps} • RIR ${l.rir}",color=MUTED,fontSize=12.sp)}}}}
}

@Composable
private fun Settings(state: AppState, save: () -> Unit, back: () -> Unit) {
    LazyColumn(contentPadding=PaddingValues(bottom=110.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){item{Header("SETTINGS","ARC preferences",back)};item{CardBox(Modifier.padding(horizontal=16.dp).fillMaxWidth()){Text("DEFAULT REST",fontWeight=FontWeight.Bold);Text("${state.defaultRest}s",fontSize=24.sp);Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){OutlinedButton({state.defaultRest=(state.defaultRest-15).coerceAtLeast(15);save()}){Text("-15")};OutlinedButton({state.defaultRest=(state.defaultRest+15).coerceAtMost(900);save()}){Text("+15")}}}};item{CardBox(Modifier.padding(horizontal=16.dp).fillMaxWidth()){Row(verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("Haptic cues",fontWeight=FontWeight.Bold);Text("Timer feedback",color=MUTED,fontSize=12.sp)};Switch(state.haptics,{state.haptics=it;save()})}}};item{CardBox(Modifier.padding(horizontal=16.dp).fillMaxWidth()){Text("ABOUT ARC",fontWeight=FontWeight.Bold);Text("Train. Track. Evolve.",color=ACCENT,fontWeight=FontWeight.Bold);Text("Exercise demonstrations are bundled locally as animated human demonstrations, so the core demo experience does not depend on a network request.",color=MUTED,fontSize=12.sp)}}}
}

private data class Guide(val setup:String,val start:String,val steps:String,val breathing:String,val tempo:String,val rom:String,val mistakes:String,val beginner:String)
private fun guideFor(ex:Exercise):Guide{val n=ex.name.lowercase();return when{n.contains("bench press")||n.contains("press")&&ex.primaryMuscle.contains("Chest",true)->Guide("Set the bench securely, place both feet firmly on the floor, grip the bar or handles evenly and keep the shoulders stable.","Start with the resistance over the intended path and wrists stacked over the forearms.","1. Brace. 2. Lower the resistance under control. 3. Pause briefly at the controlled end range. 4. Press smoothly. 5. Reset before repeating.","Inhale and brace before the lowering phase. Exhale through the main effort.","Use a controlled lowering phase and smooth effort phase.","Use the range you can control without losing position.","Bouncing, excessive momentum, poor wrist position, or too much load.","Start lighter than you think you need and learn the path first.");n.contains("lat pulldown")||n.contains("pull up")||n.contains("chin up")->Guide("Adjust the seat or grip so you are secure and choose a manageable load or assistance.","Sit or hang in the demonstrated starting position with the torso controlled.","1. Brace lightly. 2. Drive the elbows through the intended path. 3. Reach the controlled end position. 4. Return slowly.","Exhale on the pull, inhale on the return.","Smooth pull, slower return, no swinging.","Stay inside the comfortable shoulder range.","Swinging, leaning too far, pulling behind the neck, or rushing.","Think elbows first rather than yanking with the hands.");n.contains("squat")||ex.pattern=="squat"->Guide("Set the rack or machine safely and choose a stance you can balance and control.","Brace the trunk and keep the feet connected to the floor.","1. Brace. 2. Bend hips and knees together. 3. Lower with balance. 4. Drive through the floor to stand.","Inhale and brace before descending. Exhale while standing.","Controlled descent and deliberate ascent.","Use the deepest range you can control without losing balance.","Rushing, knee collapse, losing balance, or using too much weight.","Practice the unloaded pattern first.");n.contains("curl")->Guide("Choose a manageable load and keep the elbows positioned for the specific curl variation.","Begin with the arms in the demonstrated starting position.","1. Keep the upper arm stable. 2. Curl through the elbow. 3. Pause near the top. 4. Lower slowly.","Exhale on the curl and inhale on the return.","Smooth curl and controlled lowering.","Use a comfortable elbow and shoulder range.","Swinging the torso, lifting the shoulders, or cutting the return short.","Lower the weight until every rep stays controlled.");else->Guide("Set up the equipment exactly as demonstrated and verify the load is appropriate.","Start from the shown position with the non-moving body parts stable.","1. Set the body. 2. Move the resistance along its intended path. 3. Keep stabilizers controlled. 4. Return slowly. 5. Repeat.","Breathe comfortably: inhale during preparation or return, exhale during the main effort.","Controlled rhythm with no bouncing.","Use a pain-free, controlled range appropriate to the movement.","Too much load, momentum, poor setup, or changing position between reps.","Start light and build consistent technique before adding load.")}}

private fun patternLabel(p:String)=p.replace('_',' ').replaceFirstChar{it.uppercase()}
private fun Double.clean()=if(this%1.0==0.0)this.toInt().toString() else "%.1f".format(this)

private fun primaryMuscle(day:WorkoutDay,exercises:List<Exercise>):String{val c=mutableMapOf<String,Int>();day.exercises.forEach{p->exercises.firstOrNull{it.id==p.exerciseId}?.let{c[it.primaryMuscle]=(c[it.primaryMuscle]?:0)+1}};return c.maxByOrNull{it.value}?.key?:"Full Body"}

private fun demoRes(demo:String)=when(demo){"bench_press"->R.raw.bench_press;"incline_press"->R.raw.incline_press;"fly"->R.raw.fly;"row"->R.raw.row;"pulldown"->R.raw.pulldown;"squat"->R.raw.squat;"hinge"->R.raw.hinge;"shoulder_press"->R.raw.shoulder_press;"lateral_raise"->R.raw.lateral_raise;"curl"->R.raw.curl;"pushdown"->R.raw.pushdown;"calf_raise"->R.raw.calf_raise;else->R.raw.core}

@Composable
private fun CustomExerciseDialog(close:()->Unit, create:(String,String,String,String)->Unit){var n by remember{mutableStateOf("")};var m by remember{mutableStateOf("Chest")};var e by remember{mutableStateOf("Dumbbell")};var p by remember{mutableStateOf("isolation")};AlertDialog(onDismissRequest=close,title={Text("Custom exercise")},text={Column(verticalArrangement=Arrangement.spacedBy(7.dp)){OutlinedTextField(n,{n=it},label={Text("Name")});OutlinedTextField(m,{m=it},label={Text("Primary muscle")});OutlinedTextField(e,{e=it},label={Text("Equipment")});OutlinedTextField(p,{p=it},label={Text("Demo profile")})}},confirmButton={Button({if(n.isNotBlank())create(n,m,e,p)},colors=ButtonDefaults.buttonColors(containerColor=ACCENT,contentColor=Color.Black)){Text("CREATE")}},dismissButton={TextButton(close){Text("CANCEL")}})}

private fun seedExercises():List<Exercise>{
    fun e(name:String,muscle:String,equipment:String,pattern:String,difficulty:String="Beginner",secondary:List<String> = emptyList(),demo:String=demoFor(name,pattern)):Exercise=Exercise(name=name,primaryMuscle=muscle,equipment=equipment,pattern=pattern,difficulty=difficulty,secondaryMuscles=secondary,demo=demo)
    return listOf(
        e("Barbell Bench Press","Chest","Barbell","horizontal_push","Intermediate",listOf("Triceps","Front Delts")),e("Incline Barbell Bench Press","Chest","Barbell","incline_push","Intermediate",listOf("Triceps","Front Delts"),"incline_press"),e("Dumbbell Bench Press","Chest","Dumbbell","horizontal_push","Beginner",listOf("Triceps","Front Delts")),e("Incline Dumbbell Press","Chest","Dumbbell","incline_push","Beginner",listOf("Triceps","Front Delts"),"incline_press"),e("Dumbbell Fly","Chest","Dumbbell","isolation",secondary=listOf("Front Delts"),demo="fly"),e("Cable Chest Fly","Chest","Cable","isolation",secondary=listOf("Front Delts"),demo="fly"),e("Machine Chest Press","Chest","Machine","horizontal_push"),e("Push Up","Chest","Bodyweight","horizontal_push",secondary=listOf("Triceps","Front Delts")),
        e("Barbell Row","Back","Barbell","horizontal_pull","Intermediate",listOf("Biceps","Rear Delts"),"row"),e("Chest Supported Row","Back","Dumbbell","horizontal_pull",secondary=listOf("Biceps","Rear Delts"),demo="row"),e("Seated Cable Row","Back","Cable","horizontal_pull",secondary=listOf("Biceps","Rear Delts"),demo="row"),e("Lat Pulldown","Lats","Cable","vertical_pull",secondary=listOf("Biceps"),demo="pulldown"),e("Neutral Grip Lat Pulldown","Lats","Cable","vertical_pull",secondary=listOf("Biceps"),demo="pulldown"),e("Pull Up","Lats","Bodyweight","vertical_pull","Intermediate",listOf("Biceps"),"pulldown"),e("Chin Up","Lats","Bodyweight","vertical_pull","Intermediate",listOf("Biceps"),"pulldown"),e("Deadlift","Back","Barbell","hinge","Intermediate",listOf("Glutes","Hamstrings"),"hinge"),e("Dumbbell Shrug","Traps","Dumbbell","isolation",secondary=listOf("Forearms")),e("Face Pull","Rear Delts","Cable","rear_delt",secondary=listOf("Upper Back"),demo="row"),
        e("Barbell Overhead Press","Shoulders","Barbell","vertical_push","Intermediate",listOf("Triceps"),"shoulder_press"),e("Seated Dumbbell Shoulder Press","Shoulders","Dumbbell","vertical_push",secondary=listOf("Triceps"),demo="shoulder_press"),e("Arnold Press","Shoulders","Dumbbell","vertical_push","Intermediate",listOf("Triceps"),"shoulder_press"),e("Machine Shoulder Press","Shoulders","Machine","vertical_push",demo="shoulder_press"),e("Dumbbell Lateral Raise","Shoulders","Dumbbell","lateral_raise",secondary=listOf("Upper Traps"),demo="lateral_raise"),e("Cable Lateral Raise","Shoulders","Cable","lateral_raise",secondary=listOf("Upper Traps"),demo="lateral_raise"),e("Machine Lateral Raise","Shoulders","Machine","lateral_raise",secondary=listOf("Upper Traps"),demo="lateral_raise"),e("Reverse Pec Deck","Rear Delts","Machine","rear_delt",secondary=listOf("Upper Back"),demo="lateral_raise"),
        e("EZ Bar Curl","Biceps","EZ Bar","curl",secondary=listOf("Brachialis"),demo="curl"),e("Barbell Curl","Biceps","Barbell","curl",demo="curl"),e("Dumbbell Curl","Biceps","Dumbbell","curl",demo="curl"),e("Incline Dumbbell Curl","Biceps","Dumbbell","curl",demo="curl"),e("Hammer Curl","Biceps","Dumbbell","curl",secondary=listOf("Brachialis","Forearms"),demo="curl"),e("Preacher Curl","Biceps","Machine","curl",demo="curl"),e("Cable Curl","Biceps","Cable","curl",demo="curl"),e("Rope Triceps Pushdown","Triceps","Cable","triceps_pushdown",demo="pushdown"),e("Straight Bar Pushdown","Triceps","Cable","triceps_pushdown",demo="pushdown"),e("Overhead Cable Extension","Triceps","Cable","triceps_extension",demo="pushdown"),e("Skull Crusher","Triceps","EZ Bar","triceps_extension","Intermediate",demo="pushdown"),
        e("Back Squat","Quadriceps","Barbell","squat","Intermediate",listOf("Glutes","Hamstrings"),"squat"),e("Front Squat","Quadriceps","Barbell","squat","Intermediate",listOf("Glutes"),"squat"),e("Goblet Squat","Quadriceps","Dumbbell","squat",secondary=listOf("Glutes"),demo="squat"),e("Smith Machine Squat","Quadriceps","Smith Machine","squat","Intermediate",listOf("Glutes"),"squat"),e("Hack Squat","Quadriceps","Machine","squat","Intermediate",listOf("Glutes"),"squat"),e("Leg Press","Quadriceps","Machine","squat",secondary=listOf("Glutes"),demo="squat"),e("Bulgarian Split Squat","Quadriceps","Dumbbell","lunge","Intermediate",listOf("Glutes"),"squat"),e("Walking Lunge","Quadriceps","Dumbbell","lunge",secondary=listOf("Glutes"),demo="squat"),e("Leg Extension","Quadriceps","Machine","isolation",demo="squat"),e("Romanian Deadlift","Hamstrings","Barbell","hinge","Intermediate",listOf("Glutes","Back"),"hinge"),e("Dumbbell Romanian Deadlift","Hamstrings","Dumbbell","hinge","Beginner",listOf("Glutes","Back"),"hinge"),e("Seated Leg Curl","Hamstrings","Machine","isolation",demo="hinge"),e("Lying Leg Curl","Hamstrings","Machine","isolation",demo="hinge"),e("Hip Thrust","Glutes","Barbell","hinge","Beginner",listOf("Hamstrings","Quadriceps"),"hinge"),e("Glute Bridge","Glutes","Bodyweight","hinge",demo="hinge"),e("Hip Abduction","Glutes","Machine","isolation",demo="hinge"),e("Standing Calf Raise","Calves","Machine","calf_raise",demo="calf_raise"),e("Seated Calf Raise","Calves","Machine","calf_raise",demo="calf_raise"),e("Leg Press Calf Raise","Calves","Machine","calf_raise",demo="calf_raise"),
        e("Cable Crunch","Abdominals","Cable","crunch",demo="core"),e("Machine Crunch","Abdominals","Machine","crunch",demo="core"),e("Reverse Crunch","Abdominals","Bodyweight","crunch",demo="core"),e("Hanging Knee Raise","Abdominals","Bodyweight","crunch","Intermediate",demo="core"),e("Ab Wheel Rollout","Abdominals","Other","anti_rotation","Intermediate",demo="core"),e("Plank","Abdominals","Bodyweight","isometric",demo="core"),e("Side Plank","Obliques","Bodyweight","isometric",demo="core"),e("Pallof Press","Obliques","Cable","anti_rotation",demo="core"),e("Farmer Carry","Forearms","Dumbbell","carry",demo="hinge"),e("Wrist Curl","Forearms","Dumbbell","isolation",demo="curl")
    )
}

private fun demoFor(name:String,pattern:String)=when{ name.contains("bench press",true)&&name.contains("incline",true)->"incline_press";name.contains("bench press",true)->"bench_press";name.contains("fly",true)->"fly";pattern.contains("pull",true)||name.contains("pulldown",true)||name.contains("pull up",true)->if(name.contains("row",true))"row" else "pulldown";pattern=="squat"||pattern=="lunge"->"squat";pattern=="hinge"->"hinge";pattern.contains("press",true)->"shoulder_press";pattern.contains("lateral",true)||pattern=="rear_delt"->"lateral_raise";pattern.contains("curl",true)->"curl";pattern.contains("triceps",true)->"pushdown";pattern.contains("calf",true)->"calf_raise";else->"core"}

private fun warmupStep(title:String,instructions:String,seconds:Int)=WarmStep(title,instructions,seconds)
private data class WarmStep(val title:String,val instructions:String,val seconds:Int)
private data class StretchStep(val title:String,val target:String,val instructions:String)

private val warmups = mapOf(
    "Full Body" to listOf(warmupStep("Easy whole-body movement","Walk, cycle, march, or use another easy movement until you feel generally warm.",180),warmupStep("Joint preparation","Move shoulders, elbows, wrists, hips, knees and ankles through comfortable circles.",60),warmupStep("Bodyweight squat","Controlled easy squats with steady breathing.",45),warmupStep("First movement rehearsal","Practice the first major movement with light resistance.",60)),
    "Chest" to listOf(warmupStep("General heat","Easy walking or cycling.",180),warmupStep("Shoulder circles","Controlled circles, gradually increasing the comfortable range.",45),warmupStep("Scapular push-up","Move the shoulder blades under control while keeping the elbows mostly straight.",45),warmupStep("Press rehearsal","Use an empty bar or very light resistance.",60)),
    "Back" to listOf(warmupStep("General heat","Easy cardio for a few minutes.",180),warmupStep("Thoracic rotations","Rotate the upper back gently through a comfortable range.",45),warmupStep("Light pull-aparts","Use very light resistance.",45),warmupStep("Row rehearsal","Rehearse the first pulling pattern with light resistance.",60)),
    "Lats" to listOf(warmupStep("General heat","Easy cardio.",150),warmupStep("Overhead reach","Reach overhead comfortably without forcing the shoulder.",45),warmupStep("Straight-arm rehearsal","Practice shoulder movement with very light resistance.",45),warmupStep("First pull rehearsal","Perform easy vertical pulling repetitions.",60)),
    "Shoulders" to listOf(warmupStep("General heat","Easy whole-body movement.",150),warmupStep("Arm circles","Small controlled circles progressing to a comfortable range.",45),warmupStep("Wall slides","Keep the movement controlled and comfortable.",45),warmupStep("Press rehearsal","Use very light resistance.",60)),
    "Biceps" to listOf(warmupStep("General heat","Easy movement.",120),warmupStep("Wrist circles","Move the wrists comfortably.",30),warmupStep("Light curl","Perform very light curls with no swinging.",45),warmupStep("First-set rehearsal","Practice before working weight.",60)),
    "Triceps" to listOf(warmupStep("General heat","Easy movement.",120),warmupStep("Elbow/wrist prep","Small comfortable movements.",30),warmupStep("Light pushdown","Practice the elbow path with light resistance.",45),warmupStep("First-set rehearsal","Perform a light practice set.",60)),
    "Quadriceps" to listOf(warmupStep("General heat","Easy cycling or walking.",180),warmupStep("Ankle rocks","Move the knee over the foot while keeping the heel down.",45),warmupStep("Bodyweight squat","Controlled easy squats.",45),warmupStep("Leg movement rehearsal","Practice the first leg movement with light resistance.",60)),
    "Hamstrings" to listOf(warmupStep("General heat","Easy walking or cycling.",180),warmupStep("Hip hinge drill","Push the hips back while keeping the torso controlled.",45),warmupStep("Glute bridge","Slow bridge repetitions.",45),warmupStep("Hinge rehearsal","Use a very light load.",60)),
    "Glutes" to listOf(warmupStep("General heat","Easy walking or cycling.",150),warmupStep("Glute bridge","Slow controlled bridges.",45),warmupStep("Bodyweight lunge","Stable comfortable range.",45),warmupStep("Hip thrust rehearsal","Practice with minimal load.",60)),
    "Calves" to listOf(warmupStep("General heat","Easy movement.",120),warmupStep("Ankle circles","Slow circles through a comfortable range.",30),warmupStep("Bodyweight calf raise","Controlled repetitions.",45),warmupStep("First-set rehearsal","Use a light load.",60)),
    "Abdominals" to listOf(warmupStep("General heat","Easy movement.",120),warmupStep("Pelvic control","Gentle pelvic tilts with relaxed breathing.",45),warmupStep("Dead-bug rehearsal","Slow alternating controlled movement.",45),warmupStep("First core pattern","Practice the first core exercise.",60)),
    "Forearms" to listOf(warmupStep("General heat","Easy movement.",90),warmupStep("Wrist flexion/extension","Move the wrists through a comfortable range.",45),warmupStep("Grip rehearsal","Use a light load.",45),warmupStep("First-set rehearsal","Perform an easy first set.",60))
)

private val stretches = mapOf(
    "Full Body" to listOf(StretchStep("Easy walk","Whole body","Walk slowly and breathe comfortably for a few minutes."),StretchStep("Chest opener","Chest / shoulders","Gently open the chest without forcing."),StretchStep("Hip flexor","Hips","Use a comfortable split stance and gently shift forward."),StretchStep("Calf stretch","Calves","Keep the heel down and lean gently toward a stable surface.")),
    "Chest" to listOf(StretchStep("Doorway chest","Chest","Turn away gently from a doorway with the forearm supported."),StretchStep("Cross-body shoulder","Shoulders","Bring one arm across the body and hold gently.")),
    "Back" to listOf(StretchStep("Upper-back reach","Upper back","Reach forward comfortably and breathe slowly."),StretchStep("Cross-body reach","Upper back","Reach across the body and rotate gently.")),
    "Shoulders" to listOf(StretchStep("Cross-body shoulder","Shoulders","Hold one arm across the body gently."),StretchStep("Gentle triceps","Triceps","Bring one arm overhead and support the elbow without forcing.")),
    "Biceps" to listOf(StretchStep("Gentle biceps","Biceps","Place the hand on a stable surface and turn away slowly.")),
    "Triceps" to listOf(StretchStep("Overhead triceps","Triceps","Support the elbow gently without forcing.")),
    "Quadriceps" to listOf(StretchStep("Standing quad","Quadriceps","Hold a stable support and gently bring the heel toward you.")),
    "Hamstrings" to listOf(StretchStep("Gentle hamstring","Hamstrings","Extend one leg comfortably and lean from the hips.")),
    "Glutes" to listOf(StretchStep("Figure-four","Glutes","Place one ankle over the opposite leg and move gently into the stretch.")),
    "Calves" to listOf(StretchStep("Wall calf","Calves","Keep the heel down and lean gently toward the wall.")),
    "Abdominals" to listOf(StretchStep("Gentle trunk extension","Abdominals","Use a comfortable range and avoid forcing the lower back.")),
    "Forearms" to listOf(StretchStep("Wrist stretch","Forearms","Extend the arm and gently move the fingers back with the other hand."))
)
