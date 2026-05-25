import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'dart:convert';
import '../models/constitution.dart';
import 'chapter_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  Constitution? constitution;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    loadData();
  }

  Future<void> loadData() async {
    try {
      final String response = await rootBundle.loadString('assets/data/constitution.json');
      final data = await json.decode(response);
      setState(() {
        constitution = Constitution(chapters: (data as List).map((i) => Chapter.fromJson(i)).toList());
      });
    } catch (e) {
      setState(() {
        _errorMessage = 'حدث خطأ في تحميل الدستور. يرجى المحاولة مرة أخرى.';
        constitution = null;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('دستور رابطة الطلاب ذوي الإعاقة البصرية'),
      ),
      body: constitution == null
          ? Center(
              child: _errorMessage == null
                  ? const CircularProgressIndicator()
                  : Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Text(_errorMessage!, style: Theme.of(context).textTheme.bodyMedium),
                        const SizedBox(height: 20),
                        ElevatedButton(
                          onPressed: loadData,
                          child: const Text('إعادة المحاولة'),
                        ),
                      ],
                    ),
            )
          : ListView.builder(
              itemCount: constitution!.chapters.length,
              itemBuilder: (context, index) {
                final chapter = constitution!.chapters[index];
                return Semantics(
                  label: 'الباب ${index + 1}: ${chapter.chapter}',
                  button: true,
                  child: ListTile(
                    title: Text(
                      chapter.chapter,
                      style: Theme.of(context).textTheme.bodyLarge,
                      textAlign: TextAlign.right,
                    ),
                    trailing: const Icon(Icons.arrow_forward_ios),
                    onTap: () {
                      Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (context) => ChapterScreen(chapter: chapter),
                        ),
                      );
                    },
                  ),
                );
              },
            ),
    );
  }
}
