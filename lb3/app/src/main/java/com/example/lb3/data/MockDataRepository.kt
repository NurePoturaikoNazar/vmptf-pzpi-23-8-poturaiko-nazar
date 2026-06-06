package com.example.lb3.data

import com.example.lb3.models.Course
import com.example.lb3.models.Lesson
import com.example.lb3.models.Teacher

object MockDataRepository {

    val teachers: List<Teacher> = listOf(
        Teacher(
            1,
            "Олена Коваленко",
            "Full-stack розробниця з 10-річним досвідом. Працювала в Google та Amazon. Спеціалізується на React та Node.js.",
            "Web Development"
        ),
        Teacher(
            2,
            "Андрій Мельник",
            "Data Scientist з PhD у машинному навчанні. Автор курсів з Python та аналізу даних.",
            "Data Science"
        ),
        Teacher(
            3,
            "Марія Шевченко",
            "UX/UI дизайнерка з портфоліо у сфері фінтех та електронної комерції. Викладає дизайн уже 7 років.",
            "Design"
        )
    )

    val courses: List<Course> = listOf(
        Course(
            1,
            "React для початківців",
            "Повний курс з React.js від основ до реальних застосунків.",
            "Повний курс з React.js — від основ до створення реальних застосунків. Ви навчитесь працювати з компонентами, хуками, роутингом та станом.",
            "24 години",
            1299,
            1,
            0
        ),
        Course(
            2,
            "Node.js та Express",
            "Створення серверних додатків на Node.js з фреймворком Express.",
            "Створення серверних додатків на Node.js з фреймворком Express. REST API, робота з базами даних, аутентифікація.",
            "20 годин",
            1099,
            1,
            1
        ),
        Course(
            3,
            "Python для Data Science",
            "Аналіз даних, візуалізація та машинне навчання з Python.",
            "Аналіз даних, візуалізація та машинне навчання з Python. Pandas, NumPy, Matplotlib, Scikit-learn.",
            "30 годин",
            1599,
            2,
            2
        ),
        Course(
            4,
            "Основи Machine Learning",
            "Введення в машинне навчання: регресія, класифікація, кластеризація.",
            "Введення в машинне навчання: регресія, класифікація, кластеризація та нейронні мережі.",
            "36 годин",
            1899,
            2,
            3
        ),
        Course(
            5,
            "UI/UX Дизайн з Figma",
            "Проектування інтерфейсів у Figma: wireframes, прототипи, дизайн-системи.",
            "Проектування інтерфейсів у Figma: wireframes, прототипи, дизайн-системи та робота з командою.",
            "18 годин",
            999,
            3,
            4
        ),
        Course(
            6,
            "Веб-дизайн: від ідеї до продукту",
            "Повний процес створення вебсайту від аналізу ЦА до адаптивного дизайну.",
            "Повний процес створення вебсайту: аналіз ЦА, мудборди, створення макетів та адаптивний дизайн.",
            "22 години",
            1199,
            3,
            5
        )
    )

    val lessons: List<Lesson> = buildList {
        val titlesByCourse = listOf(
            listOf("Вступ до React", "JSX та компоненти", "Props та State", "Хуки: useState та useEffect", "React Router", "Робота з API"),
            listOf("Основи Node.js", "Модулі та NPM", "Express.js фреймворк", "REST API", "Middleware", "Аутентифікація"),
            listOf("Основи Python", "NumPy", "Pandas", "Візуалізація з Matplotlib", "Статистичний аналіз", "Введення в ML"),
            listOf("Що таке ML", "Лінійна регресія", "Класифікація", "Дерева рішень", "Кластеризація", "Нейронні мережі", "Проект"),
            listOf("Основи UX", "User Research", "Wireframing", "Прототипування у Figma", "Дизайн-системи"),
            listOf("Аналіз цільової аудиторії", "Мудборди", "Типографіка та колір", "Створення макету", "Адаптивний дизайн", "Фінальний проект")
        )
        var lessonId = 1
        titlesByCourse.forEachIndexed { courseIndex, titles ->
            titles.forEachIndexed { index, title ->
                add(
                    Lesson(
                        lessonId++,
                        courseIndex + 1,
                        title,
                        index + 1,
                        "https://lb3.example/lesson/$lessonId"
                    )
                )
            }
        }
    }

    fun getCourse(id: Int): Course? = courses.find { it.id == id }

    fun getTeacher(id: Int): Teacher? = teachers.find { it.id == id }

    fun getLessonsForCourse(courseId: Int): List<Lesson> =
        lessons.filter { it.courseId == courseId }.sortedBy { it.orderNum }

    fun getCoursesForTeacher(teacherId: Int): Int =
        courses.count { it.teacherId == teacherId }
}
