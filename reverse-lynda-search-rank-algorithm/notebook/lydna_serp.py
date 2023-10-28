import pandas as pd
from datetime import datetime

features_onehot = [
    'title_length',
    'description_length',
    'content_length',
    'title_word_count',
    'description_word_count',
    'content_word_count',
    'keyword_in_title',
    'keyword_in_description',
    'keyword_occurrence_in_content',
    'duration_in_mins',
    'course_age',
    'number_of_views',
    'is_advanced_level',
    'is_beginner_level',
    'is_intermediate_level'
]

def is_nan(x):
    return x!=x

def convert_nan_to_number(x, new=0):
    if is_nan(x):
        return new
    else:
        return x

def word_count_from_str(s, word = None):
    if s is None or is_nan(s):
        return 0
    else:
        if word is None:
            return len(s.strip().split())
        else:
            return s.lower().count(word.strip().lower())


def convert_time_to_minute(x):
    x = x.strip()
    t = int(x[:-1])
    name = x[-1]

    if name == 'h':
        res = t * 60
    if name == 'm':
        res = t * 1
    if name == 's':
        res = 0

    return res


def convert_duration(duration):
    d = duration.strip().split()
    length = len(d)

    if length ==1:
        res = convert_time_to_minute(d[0])

    if length ==2:
        res = convert_time_to_minute(d[0]) + convert_time_to_minute(d[1])

    return res


def build_features(df, crawled_date, keyword):

    df['title_length'] = df['title'].apply(len)
    df['description_length'] = df['description'].apply(len)
    df['content_length'] = df['content'].apply(len)

    df['title_word_count'] = df['title'].apply(word_count_from_str)
    df['description_word_count'] = df['description'].apply(word_count_from_str)
    df['content_word_count'] = df['content'].apply(word_count_from_str)

    df['keyword_in_title'] = df['title'].apply(lambda x: word_count_from_str(x, word=keyword)).apply(lambda x: 1 if x>0 else 0)
    df['keyword_in_description'] = df['description'].apply(lambda x: word_count_from_str(x, word=keyword)).apply(lambda x: 1 if x>0 else 0)
    df['keyword_occurrence_in_content'] = df['content'].apply(lambda x: word_count_from_str(x, word=keyword))


    df['duration_in_mins'] = df['duration'].apply(convert_duration)
    df['course_age'] = df['release_date'].apply(lambda rd: course_age(rd, crawled_date))
    df['number_of_views'] = df['views'].apply(convert_nan_to_number).apply(lambda x: int(str(x).replace(',','')))

    levels = pd.get_dummies(df['skill_level']).drop('Appropriate for all', axis=1)
    df['is_advanced_level'] = levels['Advanced']
    df['is_beginner_level'] = levels['Beginner']
    df['is_intermediate_level'] = levels['Intermediate']



def course_age(dt, crawled_date):
    age = datetime.strptime(crawled_date, '%Y-%m-%d') - datetime.strptime(dt, '%m/%d/%Y')
    return age.days

def load_data(path):
    pass
