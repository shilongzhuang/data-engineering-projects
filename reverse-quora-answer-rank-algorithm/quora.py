import yaml
from selenium import webdriver
from selenium.webdriver.common.keys import Keys
from bs4 import BeautifulSoup
import time
import os
from os import listdir, makedirs
from os.path import isfile, join, exists
import hashlib
from elasticsearch import Elasticsearch, helpers
from elasticsearch_dsl import Search
from datetime import datetime
import pystats as ps


class QuoraClient(webdriver.Chrome):

    quora_homepage = 'https://www.quora.com'

    def __init__(self, path='chromedriver', user_agent=None):
        options = webdriver.ChromeOptions()
        options.add_argument('--user-agent="'+ user_agent +'"')
        super(QuoraClient, self).__init__(executable_path=path, options=options)


    def visit(self, url):
        self.get(url)


    def login(self, email, password, sleep=2):
        self.get(self.quora_homepage)
        time.sleep(sleep)
        self.find_elements_by_name('email')[1].clear()
        self.find_elements_by_name('email')[1].send_keys(email)
        self.find_elements_by_name('password')[1].clear()
        self.find_elements_by_name('password')[1].send_keys(password)
        time.sleep(sleep)
        self.find_element_by_css_selector("input[type='submit'][value='Login']").click()
        time.sleep(sleep)


    def get_questions_from_single_topic(self, topic_url, scroll_down_times=3000):
        self.visit(topic_url)
        self.scroll_down(times=scroll_down_times)
        seed_page_source = self.get_page_source()
        seed_soup = create_soup(seed_page_source)
        question_links = seed_soup.find_all('a', attrs={'class': 'question_link'})
        for link in question_links:
            yield self.quora_homepage + link['href']

    def scroll_down(self, times):
        body = self.find_element_by_tag_name('body')
        while times > 0:
            body.send_keys(Keys.PAGE_DOWN)
            times = times-1


    def get_page_source(self):
        return self.page_source

    def print_user_agent(self):
        print(self.execute_script("return navigator.userAgent;"))

    def crawl(self, urls, save_dir, sleep=1, max_scroll_down_times=1000):
        if not exists(save_dir):
            makedirs(save_dir)

        if isinstance(urls, str) and not isinstance(urls, list):
            urls = [urls]

        for i in range(len(urls)):
            url = urls[i]
            time.sleep(sleep)
            self.visit(url)
            self.scroll_down(max_scroll_down_times)
            save_file_path = save_dir + to_hash(url) + '.txt'
#            save_file_path = save_dir + str(i+1) + '.txt'
#           overwrite control
            html = self.get_page_source()
            write_content_to_file(save_file_path, html)


    def shutdown(self):
        self.close()



class ElasticSearchClient():

    def __init__(self, host='localhost', port=9200):
        self.host = host
        self.port = port

    def connect(self):
        _es = None
        _es = Elasticsearch([{'host': self.host, 'port': self.port}])

        if _es.ping():
            print('Connected!')
        else:
            pring('Could not connect!')
        self.es = _es
        return _es

    @staticmethod
    def is_exist(es, index_name):
        return es.indices.exists(index_name)

    @staticmethod
    def create_mapping(es, mapping, index_name):
        created = False

        try:
            if not ElasticSearchClient.is_exist(es, index_name):
                es.indices.create(index=index_name, ignore=400, body=mapping)
            created = True
        except Exception as e:
            print(str(e))

        finally:
            return created

    @staticmethod
    def bulk(client, actions, stats_only=False, *args, **kwargs):
        helpers.bulk(client=client, actions=actions, stats_only=stats_only, *args, **kwargs)

    @staticmethod
    def to_table_from_query(query):
        return ps.PyTable(hit.to_dict() for hit in query.scan())

    @staticmethod
    def to_table_from_index(es, index_name):
        query = Search(using=es, index=index_name)
        return ElasticSearchClient.to_table_from_query(query)


"""
helpers
"""

def to_hash(url, algorithm='md5'):
    return hashlib.md5(url.encode()).hexdigest()


def create_soup(html_page_source, type='html.parser'):
    return BeautifulSoup(html_page_source, 'html.parser')


def get_credential(config='conf.yaml'):
    with open(config, 'r+') as c:
        conf = yaml.load(c)
        c.close()
    return conf['login']['email'], conf['login']['password']


def get_questions_list(file_path):
    questions = []
    with open(file_path, 'r+', encoding='utf-8') as f:
        for line in f.readlines():
            questions.append(line.rstrip('\n'))
        f.close()
    return questions


def write_content_to_file(file_path_to, content, encoding='utf-8'):
    with open(file_path_to, 'w+', encoding=encoding) as f:
        f.write(content)
        f.close()


def read_content_from_file(file_path_from, encoding='utf-8'):
    with open(file_path_from, 'r+', encoding=encoding) as f:
        content = f.read()
        f.close()
    return content


def list_all_files(dir_path):
    files = [dir_path + f for f in listdir(dir_path) if isfile(join(dir_path, f))]
    return files


def get_all_htmls(files):
    htmls = []
    for file in files:
        html = read_content_from_file(file_path_from = file)
        htmls.append(html)
    return htmls


def is_not_none(x):
    return x is not None


"""
generator
"""

def answers_info_gen(path='data/final_questions.txt'):

    with open(path, 'r+', encoding='utf-8') as f:

        for line in f.readlines():
            line = line.rstrip('\n')
            html = read_content_from_file(line)
            created_date = datetime.fromtimestamp(os.path.getctime(line)).strftime('%Y-%m-%d')
            crawled_date = datetime.fromtimestamp(os.path.getmtime(line)).strftime('%Y-%m-%d')

            answer_info = dict()
            soup = create_soup(html)

            question_url = soup.find('link', attrs={'rel': 'canonical'})['href'].strip()
            question = soup.find('span', attrs={'class' : 'rendered_qtext'}).text
            answers = int(soup.find('div', attrs={'class': 'answer_count'}).text.split()[0].rstrip('+'))
            items = soup.find_all('div', attrs={'class': 'Answer AnswerBase'})

            for index, item in enumerate(items):

                rank = index+1

                user_url = None
                author = None
                user = item.find('a', attrs={'class': 'user'})
                if user is not None:
                    user_url = QuoraClient.quora_homepage + user['href']
                    author = user.text

                answer_date = item.find('a', attrs={'class': 'answer_permalink'}).text

                views = item.find('span', attrs={'class': 'meta_num'})
                if views is not None:
                    views = views.text
                else:
                    views = 0

                content = item.find('div', attrs={'class': 'u-serif-font-main--large'})

                button = item.find('div', attrs={'class': 'ui_button_label_count_wrapper'})
                upvotes = button.find('span', attrs={'class': 'ui_button_count_static'})

                if upvotes is None:
                    upvotes = button.find('span', attrs={'class': 'ui_button_count_optimistic_count'}).text
                else:
                    upvotes = upvotes.text

                number_of_a_tags = 0
                atags = content.find_all('a')
                if atags is not None:
                    number_of_a_tags = len(atags)


                number_of_images = 0
                imgtags = content.find_all('img')
                if imgtags is not None:
                    number_of_images = len(imgtags)


                answer_info['_id'] = to_hash(question_url + str(rank))
                answer_info['created_date'] = created_date
                answer_info['crawled_date'] = crawled_date
                answer_info['question_url'] = question_url
                answer_info['question'] = question
                answer_info['answers'] = answers
                answer_info['rank'] = rank
                answer_info['user_url'] = user_url
                answer_info['author'] = author
                answer_info['answer_date'] = answer_date
                answer_info['views'] = views
                answer_info['upvotes'] = upvotes
                answer_info['content'] = content.text
                answer_info['number_of_a_tags'] = number_of_a_tags
                answer_info['number_of_images'] = number_of_images
                yield answer_info
        f.close()


def users_info_gen(dir='data/users/'):

    user_files = list_all_files(dir)

    for user_file in user_files:
        user_info = dict()

        user_html = read_content_from_file(user_file)
        user_soup = create_soup(user_html)

        user_url = user_soup.find('link', attrs={'rel': 'canonical'})['href'].strip()
    #     try:
    #         user_url = user_soup.find('link', attrs={'rel': 'canonical'})['href'].strip()
    #     except:
    #         continue
        highlight = user_soup.find('div', attrs={'class': 'AnswerViewsAboutListItem'})

        answer_views = None
        answer_views_this_month = None

        if highlight is not None:
            answer_views = highlight.find('span', attrs={'class': 'main_text'}).text
            answer_views_this_month = highlight.find('span', attrs={'class': 'detail_text'}).text

        answers = user_soup.find('li', attrs={'class': 'AnswersNavItem'}).find('span', attrs={'class': 'list_count'}).text
        questions = user_soup.find('li', attrs={'class': 'QuestionsNavItem'}).find('span', attrs={'class': 'list_count'}).text
        posts = user_soup.find('li', attrs={'class': 'PostsNavItem'}).find('span', attrs={'class': 'list_count'}).text
        blogs = user_soup.find('li', attrs={'class': 'BlogsNavItem'}).find('span', attrs={'class': 'list_count'}).text
        followers = user_soup.find('li', attrs={'class': 'FollowersNavItem'}).find('span', attrs={'class': 'list_count'}).text
        following = user_soup.find('li', attrs={'class': 'FollowingNavItem'}).find('span', attrs={'class': 'list_count'}).text
        topics = user_soup.find('li', attrs={'class': 'TopicsNavItem'}).find('span', attrs={'class': 'list_count'}).text

        user_info['_id'] = user_file.split('/')[2].split('.txt')[0]
        user_info['user_file_name'] = user_file
        user_info['user_url'] = user_url
        user_info['answer_views'] = answer_views
        user_info['answer_views_this_month'] = answer_views_this_month
        user_info['answers'] = answers
        user_info['questions'] = questions
        user_info['posts'] = posts
        user_info['followers'] = followers
        user_info['following'] = following
        user_info['topics'] = topics
        yield user_info


def load_es_mapping(type):
    if type=='anwser':
        path = 'mapping_answer.txt'
    if type=='user':
        path = 'mapping_user.text'
    return read_content_from_file(path)


"""
data clean up
"""


def clean_up_anwser_views(a):
    res = 0
    if a is None:
        res = 0
    else:
        a = a.strip().split()[0]
        if a[-1] =='k':
            res = float(a[:-1]) * 1000
        elif a[-1] =='m':
            res =float(a[:-1]) * 1000000
        else:
            res = a

    return int(res)

def from_string_to_number(str, char=','):
    return int(str.replace(char,''))


def build_user_table(old_table):
    new_table = ps.PyTable()
    new_table['user_url'] = old_table['user_url']
    new_table['answer_views'] = old_table['answer_views'].apply(clean_up_anwser_views)
    new_table['answer_views_this_month'] = old_table['answer_views_this_month'].apply(clean_up_anwser_views)
    new_table['answers'] = old_table['answers'].apply(from_string_to_number)
    new_table['followers'] = old_table['followers'].apply(from_string_to_number)
    new_table['following'] = old_table['following'].apply(from_string_to_number)
    new_table['posts'] = old_table['posts'].apply(from_string_to_number)
    new_table['questions'] = old_table['questions'].apply(from_string_to_number)
    new_table['topics'] = old_table['topics'].apply(from_string_to_number)
    return new_table


def clean_up_vote_or_view(a):
    res = 0
    if a is None:
        res = 0
    else:
        a = str(a).strip()
        if a[-1] =='k':
            res = float(a[:-1]) * 1000
        elif a[-1] =='m':
            res =float(a[:-1]) * 1000000
        else:
            res = a

    return int(res)


def days_since_last_modified(ad, crawled_date):
    """
    1. '11h ago, 2018'
    2. Sun
    3. Answered Sun
    """
    crawled_datetime = datetime.strptime(crawled_date, '%Y-%m-%d')
    crawled_weekday = crawled_datetime.weekday()


    weekdays = {
        'Mon': 0,
        'Tue': 1,
        'Wed': 2,
        'Thu': 3,
        'Fri': 4,
        'Sat': 5,
        'Sun': 6
    }

    s = ad.strip().split()[0]

    res = ad.replace(s,'').strip()

    if 'ago' in res:
        return 0

    if len(res) == 3:
        if crawled_weekday >= weekdays[res]:
            return (crawled_weekday - weekdays[res])
        else:
            return (7 + crawled_weekday - weekdays[res])
    else:
        if ',' not in res:
            res = res + ', ' + str(datetime.now().year)

    res = crawled_datetime - datetime.strptime(res, '%b %d, %Y')

    return res.days

def is_nan(x):
    return x!=x

def word_count_from_str(s, word = None):
    if s is None or is_nan(s):
        return 0
    else:
        if word is None:
            return len(s.strip().split())
        else:
            return s.lower().count(word.strip().lower())

def build_anwser_table(old_table):
    new_table = ps.PyTable()
    new_table['question_url'] = old_table['question_url']
    new_table['user_url'] = old_table['user_url']
    new_table['answers'] = old_table['answers']
    new_table['rank'] = old_table['rank']
    new_table['number_of_a_tags'] = old_table['number_of_a_tags']
    new_table['number_of_images'] = old_table['number_of_images']
    new_table['views'] = old_table['views'].apply(clean_up_vote_or_view)
    new_table['upvotes'] = old_table['upvotes'].apply(clean_up_vote_or_view)
    new_table['content_word_count'] = old_table['content'].apply(word_count_from_str)
    new_table['days_since_last_modified'] = old_table.apply(lambda row: days_since_last_modified(row['answer_date'], row['crawled_date']), axis=1)
    return new_table
