package com.arc.training

import java.io.Serializable
import java.util.UUID

private fun stableId(name: String, muscle: String, equipment: String, pattern: String): String =
    "arc_" + listOf(name, muscle, equipment, pattern).joinToString("|")
        .lowercase()
        .replace(Regex("[^a-z0-9]+"), "_")
        .trim('_')

data class Exercise(
    val name: String,
    val primaryMuscle: String,
    val equipment: String,
    val pattern: String = "isolation",
    val difficulty: String = "beginner",
    val secondaryMuscles: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val demo: String = "core",
    val id: String = stableId(name, primaryMuscle, equipment, pattern),
    val isCustom: Boolean = false
) : Serializable

data class PlannedExercise(
    val exerciseId: String,
    var sets: Int = 3,
    var repsMin: Int = 8,
    var repsMax: Int = 12,
    var weight: Double = 0.0,
    var restSeconds: Int = 90,
    var rir: Int = 2,
    var rpe: Double = 8.0,
    var tempo: String = "2-1-2",
    var warmupSets: Int = 1,
    var notes: String = ""
) : Serializable

data class WorkoutDay(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var restDay: Boolean = false,
    val exercises: MutableList<PlannedExercise> = mutableListOf()
) : Serializable

data class WorkoutPlan(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    val days: MutableList<WorkoutDay> = mutableListOf()
) : Serializable

data class LoggedSet(
    val exerciseId: String,
    val weight: Double,
    val reps: Int,
    val rir: Int,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

data class WorkoutSession(
    val id: String = UUID.randomUUID().toString(),
    val planId: String,
    val dayId: String,
    val startedAt: Long,
    val endedAt: Long? = null,
    val sets: MutableList<LoggedSet> = mutableListOf()
) : Serializable

data class Goal(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var type: String = "custom",
    var target: Double = 1.0,
    var current: Double = 0.0,
    var unit: String = "",
    var active: Boolean = true
) : Serializable

data class ChecklistItem(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var done: Boolean = false
) : Serializable

data class Checklist(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    val items: MutableList<ChecklistItem> = mutableListOf()
) : Serializable

data class AppState(
    val plans: MutableList<WorkoutPlan> = mutableListOf(),
    val sessions: MutableList<WorkoutSession> = mutableListOf(),
    val goals: MutableList<Goal> = mutableListOf(),
    val checklists: MutableList<Checklist> = mutableListOf(),
    val customExercises: MutableList<Exercise> = mutableListOf(),
    val favorites: MutableSet<String> = mutableSetOf(),
    val recentExerciseIds: MutableList<String> = mutableListOf(),
    var selectedPlanId: String? = null,
    var selectedDayId: String? = null,
    var defaultRest: Int = 90,
    var weightUnit: String = "kg",
    var currentStreak: Int = 0,
    var haptics: Boolean = true,
    var darkMode: Boolean = true
) : Serializable
