Демьянова Алёна Владиславовна Б9123-09.03.03 пикд8
Countries explorer (REST Countries) - со списком всех стран мира и подробными данными по каждой стране (название, код, столица, регион, население, флаг и т.д.).
API поддерживает поиск по имени и коду страны, а также фильтрацию полей через параметр fields, что позволяет удобно получать данные для экрана списка и экрана деталей.
API‑ключ не требуется, но для запуска нужно включить впн
Чек‑лист
Обязательное
Навигация: экран списка/поиска стран, экран деталей(с кнопкой "назад") плюс отдельный экран избранного
Архитектура:
UiState (Loading / Empty / Error / Success)
ViewModel для списка и деталей
UI‑экраны получают state + callbacks
Repository между ViewModel и Retrofit
Coroutines + Retrofit:
все запросы как suspend‑функции в CountriesApi
вызовы идут из viewModelScope.launch
UI‑состояния:
Loading – индикатор загрузки
Error – текст ошибки
Empty – сообщение «Ничего не найдено»
Success – список стран / экран деталей
Избранное (без БД):
можно добавлять/удалять страны в favourites
список избранного на отдельном экране
избранное хранится в ViewModel и даже переживает поворот экрана
![photo_5467714546198843548_y](https://github.com/user-attachments/assets/14651c14-89bd-457e-a833-93e5193cb1b6)
![photo_5467714546198843600_y](https://github.com/user-attachments/assets/1ffc4d4d-fa21-4c96-aad9-7e0c91b205d5)
![photo_5467714546198843530_y](https://github.com/user-attachments/assets/a46c6d5b-1776-4990-b9b1-183ca019a42a)
![photo_5467714546198843531_y](https://github.com/user-attachments/assets/1561e723-abc7-4625-984f-4648b0da8f6f)
![photo_5467714546198843532_y](https://github.com/user-attachments/assets/31c93208-9db1-433a-9560-4627508dc8a9)
