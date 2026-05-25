class Constitution {
  final List<Chapter> chapters;

  Constitution({required this.chapters});

  factory Constitution.fromJson(Map<String, dynamic> json) {
    var list = json['chapters'] as List;
    List<Chapter> chapterList = list.map((i) => Chapter.fromJson(i)).toList();
    return Constitution(chapters: chapterList);
  }
}

class Chapter {
  final String chapter;
  final List<Article> articles;

  Chapter({required this.chapter, required this.articles});

  factory Chapter.fromJson(Map<String, dynamic> json) {
    var list = json['articles'] as List;
    List<Article> articleList = list.map((i) => Article.fromJson(i)).toList();
    return Chapter(chapter: json['chapter'], articles: articleList);
  }
}

class Article {
  final int id;
  final String title;
  final String content;

  Article({required this.id, required this.title, required this.content});

  factory Article.fromJson(Map<String, dynamic> json) {
    return Article(
      id: json['id'],
      title: json['title'],
      content: json['content'],
    );
  }
}
