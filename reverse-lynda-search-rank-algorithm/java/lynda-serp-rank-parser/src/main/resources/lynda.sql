/*
1. create a new database
2. create a user for this projec
 */
SET search_path TO website;
SET search_path TO lynda;
GRANT CONNECT ON DATABASE website TO szhuang;
GRANT USAGE ON SCHEMA lynda TO szhuang;
GRANT ALL ON SCHEMA lynda TO szhuang;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA lynda TO szhuang;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA lynda TO szhuang;
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA lynda TO szhuang;

/*
create a table to maintain serp for course (e.g. rank, url)
 */
DROP TABLE IF EXISTS lynda.course_serp;
CREATE TABLE lynda.course_serp (
  id SERIAL NOT NULL PRIMARY KEY,
  keyword VARCHAR(100)  NULL,
  course_id VARCHAR(100) NOT NULL,
  url VARCHAR(300) NOT NULL,
  rank INTEGER NOT NULL,
  page_number INTEGER NOT NULL
);

/*
create a table to maintain all information to each course (course id, content)
 */
DROP TABLE IF EXISTS lynda.course_info;
CREATE TABLE lynda.course_info (
  id SERIAL NOT NULL PRIMARY KEY,
  course_id VARCHAR(100) NOT NULL,
  title VARCHAR(100),
  description VARCHAR(300),
  skill_level VARCHAR(100),
  duration VARCHAR(10),
  views VARCHAR(20),
  release_date VARCHAR(10),
  content TEXT,
  author VARCHAR(100),
  author_profile VARCHAR(200),
  exercise_file_size VARCHAR(10),
  category VARCHAR(100),
  sub_category VARCHAR(100)
);


SELECT
  course_id,
  count(*)
FROM lynda.course_serp
GROUP BY course_id
ORDER BY count(*) DESC;

/*
export the data for analysis
 */
SELECT
  a.course_id,
  a.keyword,
  a.rank,
  a.page_number,
  a.url,
  b.title,
  b.description,
  b.content,
  b.duration,
  b.skill_level,
  b.release_date,
  b.views,
  b.category,
  b.sub_category,
  b.author,
  b.author_profile
FROM lynda.course_serp a
LEFT OUTER JOIN lynda.course_info b
  ON a.course_id = b.course_id
WHERE 1=1
AND keyword = 'python'
ORDER BY a.rank;