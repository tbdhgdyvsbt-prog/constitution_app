import 'package:flutter/material.dart';
import 'screens/home_screen.dart';

void main() {
  runApp(const ConstitutionApp());
}

class ConstitutionApp extends StatelessWidget {
  const ConstitutionApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'دستور الرابطة',
      debugShowCheckedModeBanner: false,
      // High Contrast Theme for visually impaired users
      theme: ThemeData(
        brightness: Brightness.dark,
        primarySwatch: Colors.yellow,
        scaffoldBackgroundColor: Colors.black,
        textTheme: const TextTheme(
          bodyLarge: TextStyle(fontSize: 24, color: Colors.white, fontWeight: FontWeight.bold),
          bodyMedium: TextStyle(fontSize: 20, color: Colors.white70),
        ),
        appBarTheme: const AppBarTheme(
          backgroundColor: Colors.yellow,
          foregroundColor: Colors.black,
          centerTitle: true,
          titleTextStyle: TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: Colors.black),
        ),
      ),
      home: const HomeScreen(),
    );
  }
}
