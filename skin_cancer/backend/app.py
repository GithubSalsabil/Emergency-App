from flask import Flask, request, jsonify
from PIL import Image
import torch
import torchvision.transforms as transforms

# Initialiser l'application Flask
app = Flask(__name__)

# Charger le modèle TorchScript
MODEL_PATH = "efficientnet_model.pt"  # Chemin vers votre modèle téléchargé
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

# Charger le modèle
try:
    model = torch.jit.load(MODEL_PATH)
    model = model.to(device)
    model.eval()
    print("Modèle chargé avec succès.")
except Exception as e:
    print(f"Erreur lors du chargement du modèle : {e}")
    exit()

# Définir les classes (par exemple, 0 = benign, 1 = malignant)
classes = ["benign", "malignant"]

# Définir les transformations pour les images
transform = transforms.Compose([
    transforms.Resize((224, 224)),
    transforms.ToTensor(),
    transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])
])

@app.route("/predict", methods=["POST"])
def predict():
    # Vérifier si un fichier est bien envoyé
    if "file" not in request.files:
        return jsonify({"error": "Aucun fichier envoyé"}), 400

    file = request.files["file"]

    try:
        # Charger l'image depuis le fichier envoyé
        image = Image.open(file.stream).convert("RGB")

        # Appliquer les transformations
        input_tensor = transform(image).unsqueeze(0).to(device)

        # Effectuer la prédiction
        with torch.no_grad():
            output = model(input_tensor)  # Le modèle retourne une sortie
            probabilities = torch.sigmoid(output).squeeze().cpu().numpy()  # Appliquer sigmoid

        # Si output est un tableau 0-dimension (un seul élément)
        if probabilities.ndim == 0:
            probabilities = probabilities.reshape(1)  # Le convertir en tableau 1D

        # Trouver la classe prédite et son score
        predicted_class = classes[int(probabilities > 0.5)]  # Seuil à 0.5 pour la classification binaire
        confidence_score = probabilities[0]  # Prendre la confiance de la classe prédite

        # Retourner le résultat sous forme de JSON
        return jsonify({
            "prediction": predicted_class,
            "confidence": float(confidence_score)
        }), 200

    except Exception as e:
        return jsonify({"error": str(e)}), 500

# Lancer le serveur Flask
if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)
