import 'dart:convert';
import 'dart:io';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:http/http.dart' as http;

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Skin Cancer Detection',
      theme: ThemeData(
        primarySwatch: Colors.teal,
        useMaterial3: true,
      ),
      home: const SkinDiagnosisScreen(),
    );
  }
}

class SkinDiagnosisScreen extends StatefulWidget {
  const SkinDiagnosisScreen({super.key});

  @override
  _SkinDiagnosisScreenState createState() => _SkinDiagnosisScreenState();
}

class _SkinDiagnosisScreenState extends State<SkinDiagnosisScreen> {
  File? _image;
  String _prediction = '';
  double _confidence = 0.0;
  final ImagePicker _picker = ImagePicker();

  // Function to capture an image using the camera
  Future<void> _captureImage() async {
    final XFile? image = await _picker.pickImage(source: ImageSource.camera);
    if (image != null) {
      setState(() {
        _image = File(image.path);
      });
    }
  }

  // Function to select an image from the gallery
  Future<void> _selectImage() async {
    final XFile? image = await _picker.pickImage(source: ImageSource.gallery);
    if (image != null) {
      setState(() {
        _image = File(image.path);
      });
    }
  }

  // Function to send the image to the Flask API for prediction
  Future<void> _detectSkinCancer() async {
    if (_image == null) return;

    final Uri apiUrl = Uri.parse('http://192.168.43.193:5000/predict'); // Replace with your Flask server's IP
    final request = http.MultipartRequest('POST', apiUrl)
      ..files.add(await http.MultipartFile.fromPath('file', _image!.path));

    try {
      final response = await request.send();
      final responseBody = await response.stream.bytesToString();
      final data = json.decode(responseBody);

      if (data['error'] != null) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(data['error'])));
      } else {
        setState(() {
          _prediction = data['prediction'];
          _confidence = data['confidence'];
        });
      }
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Failed to detect: $e')));
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text(
          'Skin Cancer',
          style: TextStyle(color: Colors.black, fontSize: 24),
        ),

      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [

            const SizedBox(height: 10),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                ElevatedButton.icon(
                  onPressed: _selectImage,

                  label: const Text('SELECT PHOTO'),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.white30,
                    minimumSize: const Size(150, 50),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(30)),
                  ),
                ),
                ElevatedButton.icon(
                  onPressed: _captureImage,

                  label: const Text('START CAMERA'),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.white30,
                    minimumSize: const Size(150, 50),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(30)),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 20),
            Expanded(
              child: Container(

                child: _image != null
                    ? ClipRRect(
                  child: Image.file(
                    _image!,
                    fit: BoxFit.cover,
                  ),
                )
                    : const Center(
                  child: Text('Image view', style: TextStyle(color: Colors.grey, fontSize: 18)),
                ),
              ),
            ),
            const SizedBox(height: 20),
            ElevatedButton(
              onPressed: _detectSkinCancer,
              child: const Text('DETECT'),
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.white30,
                minimumSize: const Size(double.infinity, 50),
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(30)),
              ),
            ),
            const SizedBox(height: 20),
            // Prediction Space - Display the result
            if (_prediction.isNotEmpty && _confidence > 0)
              Container(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Prediction: $_prediction',
                      style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                    ),
                    const SizedBox(height: 10),
                    Text(
                      'Confidence: ${_confidence.toStringAsFixed(2)}%',
                      style: const TextStyle(fontSize: 16, color: Colors.black),
                    ),
                  ],
                ),
              ),
          ],
        ),
      ),
    );
  }
}
