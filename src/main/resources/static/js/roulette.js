let currentUser = null;

// Функция для получения текущего пользователя
function initializeCurrentUser() {
    return fetch('/game/currentUser')
        .then(response => {
            if (!response.ok) {
                throw new Error('Не удалось получить пользователя');
            }
            return response.json();
        })
        .then(user => {
            currentUser = user;
        })
        .catch(error => console.error('Ошибка:', error));
}

// Функция для получения и отображения списка групп
function fetchGroups() {
    fetch('/game/groups')
        .then(response => response.json())
        .then(groups => {
            populateTable(groups);
        })
        .catch(error => console.error('Ошибка:', error));
}

// Функция для заполнения таблицы
function populateTable(groups) {
    const tableBody = document.getElementById("groupList");
    tableBody.innerHTML = ""; // Очищаем предыдущие записи

    groups.forEach(group => {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${group.id}</td>
            <td>${group.players.length} игроков</td>
            <td><button onclick="joinGroup('${group.id}', this.closest('tr'))">Присоединиться</button></td>
        `;
        tableBody.appendChild(row); // Добавляем строку в таблицу
    });
}

// Функция для присоединения к группе
window.joinGroup = function (groupId, currentRow) {
    initializeCurrentUser()
        .then(() => {
            if (!currentUser) {
                alert("Вам надо авторизоваться в системе");
                return Promise.reject("Пользователь не авторизован.");
            }

            return fetch(`/game/join`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ groupId: groupId })
            });
        })
        .then(response => {
            if (!response.ok) {
                return response.json().then(data => {
                    // Обработка ошибок
                    if (data.status === "5") {
                        alert('Пользователь не найден. Пожалуйста, авторизуйтесь заново.');
                    } else if (data.status === "6") {
                        alert('Недостаточно средств на балансе. Пополните баланс и попробуйте снова.');
                    } else {
                        alert('Неизвестная ошибка. Попробуйте еще раз.');
                    }
                    throw new Error(`Ошибка: ${data.status}`);
                });
            }
            socket.send(`addPlayer:${groupId}:${currentUser.username}`);
            return response.json(); // Вернем успешный JSON
        })
        .then(result => {
            return fetch(`/game/group/${result.groupId}`)
                .then(response => {
                    if (!response.ok) {
                        throw new Error(`Ошибка HTTP: ${response.status}`);
                    }
                    return response.json();
                })
                .then(data => {
                    if (data.players) {
                        const playerNames = data.players.map(player => player.name);
                        if(!groupsState[groupId]){
                            initializeGroup(data.id, playerNames, data.size);
                        }
                        drawWheel(playerNames,data.size);
                        const cells = currentRow.getElementsByTagName('td');
                        cells[1].textContent = data.players.length; // Обновляем количество игроков
                        fetchGroups();
                        const userId = document.getElementById('userId').textContent;
                        const startButton = document.getElementById(`startButton-${data.id}`);
                        if (data.createUserId === userId) {
                            startButton.style.display = (data.players.length < group.size) ? 'none' : 'block';
                        } else {
                            startButton.style.display = 'none';
                        }
                    } else {
                        console.error('Ошибка: Данные не содержат игроков.');
                    }
                })
                .catch(error => console.error("Ошибка при загрузке данных группы:", error));
            fetchGroups(); // Обновляем список групп после успешного добавления
        })
        .catch(error => console.error("Error:", error));
};


// Запускаем обновление списка групп при загрузке страницы
window.onload = fetchGroups;
