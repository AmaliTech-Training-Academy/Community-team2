from abc import ABC, abstractmethod

class DataSource(ABC):

    @abstractmethod
    def get_users(self):
        pass

    @abstractmethod
    def get_posts(self):
        pass

    @abstractmethod
    def get_comments(self):
        pass