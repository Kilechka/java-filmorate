DROP TABLE IF EXISTS films, mpa, genre, users, film_genre, film_likes, friendship, films;

CREATE TABLE IF NOT EXISTS users (
  user_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  login varchar(50) NOT NULL,
  email varchar(50) NOT NULL,
  birthday date NOT NULL,
  name varchar(50)
);

CREATE TABLE IF NOT EXISTS friendship (
  user_id INTEGER NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
  friend_id INTEGER NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
  PRIMARY KEY (user_id, friend_id)
);

CREATE TABLE IF NOT EXISTS films (
  film_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  name varchar(100) NOT NULL,
  description varchar(200) NOT NULL,
  releaseDate date NOT NULL,
  duration integer NOT NULL,
  mpa_id integer NOT NULL
);

CREATE TABLE IF NOT EXISTS film_likes (
  film_id integer NOT NULL REFERENCES films (film_id) ON DELETE CASCADE,
  user_id integer NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
  PRIMARY KEY (film_id, user_id)
);

CREATE TABLE IF NOT EXISTS genre (
  genre_id integer GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  name varchar(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS mpa (
  mpa_id integer GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  name varchar(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS film_genre (
  film_id integer NOT NULL REFERENCES films (film_id) ON DELETE CASCADE,
  genre_id integer NOT NULL REFERENCES genre (genre_id) ON DELETE CASCADE,
  PRIMARY KEY (film_id, genre_id)
);

ALTER TABLE films ADD FOREIGN KEY (mpa_id) REFERENCES mpa (mpa_id);

ALTER TABLE friendship ADD FOREIGN KEY (user_id) REFERENCES users (user_id);

ALTER TABLE friendship ADD FOREIGN KEY (friend_id) REFERENCES users (user_id);


