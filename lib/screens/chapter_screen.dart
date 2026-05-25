import 'package:flutter/material.dart';
import '../models/constitution.dart';

class ChapterScreen extends StatelessWidget {
  final Chapter chapter;

  const ChapterScreen({super.key, required this.chapter});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(chapter.chapter),
      ),
      body: ListView.builder(
        padding: const EdgeInsets.all(16.0),
        itemCount: chapter.articles.length,
        itemBuilder: (context, index) {
          final article = chapter.articles[index];
          return Semantics(
            label: 'المادة ${article.id}: ${article.title}',
            child: Padding(
              padding: const EdgeInsets.only(bottom: 24.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  Text(
                    'المادة (${article.id}): ${article.title}',
                    style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                      color: Colors.yellow,
                    ),
                    textAlign: TextAlign.right,
                  ),
                  const SizedBox(height: 8),
                  Text(
                    article.content,
                    style: Theme.of(context).textTheme.bodyMedium,
                    textAlign: TextAlign.right,
                  ),
                  const Divider(color: Colors.white24, thickness: 1),
                ],
              ),
            ),
          );
        },
      ),
    );
  }
}
