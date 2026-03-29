Демьянова Алёна Владиславовна, Б9123-09.03.03, пикд2
 Countries Explorer (REST Countries) - приложение со списком стран, поиском, деталями и избранным.
 
Домашнее задание 6
 Что сделано
- Добавлен ручной "refresh/retry" 
- Список стран теперь собирается из нескольких независимых источников:
  *строка поиска
  *выбранный фильтр (все/только избранное)
  *поток действий пользователя (SharedFlow для refresh/retry)
  *избранные страны из Room (Flow)
  *настройки сортировки из DataStore (Flow)
- Избранное из Room автоматически влияет на UI без ручной перезагрузки
- Добавлена сортировка списка по названию и по населению
- Выбранная сортировка сохраняется в DataStore и влияет и на главный список, и на экран избранного
- Экран избранного тоже переведён на реактивное обновление
- Обновлены тесты под новую логику

Где используется Flow
Flow используется не просто как замена обычной переменной, а для объединения данных и событий
- StateFlow:
  - текст поиска
  - выбранный фильтр
  - текущее состояние экрана
- SharedFlow:
  - действия пользователя для refresh/retry

Использованные операторы Flow
combine — объединение нескольких потоков в итоговое состояние экрана
debounce — задержка поиска при вводе текста
distinctUntilChanged — чтобы не выполнять одинаковый поиск повторно
flatMapLatest — отмена предыдущего запроса при новом вводе или refresh
map — преобразование данных из Room/DataStore
catch — обработка ошибок в потоке
stateIn — преобразование потока в StateFlow для UI

<img width="1200" height="1920" alt="image" src="https://github.com/user-attachments/assets/58e0fdf8-48cc-4783-89d4-db2474e81ec2" />
<img width="1200" height="1920" alt="image" src="https://github.com/user-attachments/assets/c4b94dd3-1749-4ef1-8a90-97f40b439191" />
<img width="1200" height="1920" alt="image" src="https://github.com/user-attachments/assets/0f02eb43-fa03-4224-b864-487324cdba41" />
<img width="1200" height="1920" alt="image" src="https://github.com/user-attachments/assets/33a1c7d4-2cfa-456f-92b8-cd9aa24de8ba" />


![Снимок экрана 2026-03-29 224132](https://github.com/user-attachments/assets/1170ae36-7d6c-49b3-b36e-3be77b252a9a)



