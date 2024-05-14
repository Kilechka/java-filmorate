# java-filmorate
Template repository for Filmorate project.
![ER-диаграмма](https://github.com/Kilechka/java-filmorate/blob/main/src/main/resources/images/ER-%D0%B4%D0%B8%D0%B0%D0%B3%D1%80%D0%B0%D0%BC%D0%BC%D0%B0.png)

## Примеры запросов для базы данных:
#### **1. Просмотр всех фильмов**
```
SELECT film_id,
       name,
       description,
       releaseDate,
       duration
FROM films;
```
#### **2. Просмотр всех пользователей**
```
SELECT user_id,
       name,
       login,
       email,
       birthday
FROM users;
```
#### **3. Просмотр друзей пользователя с id = 1**
```
SELECT u.name
FROM users AS u
JOIN frienship AS f ON u.user_id = f.friend_id
WHERE f.user_id = 1;
```
       
       
