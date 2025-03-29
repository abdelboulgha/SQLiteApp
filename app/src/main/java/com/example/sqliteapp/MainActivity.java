package com.example.sqliteapp;



import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.example.sqliteapp.classes.Etudiant;
import com.example.sqliteapp.service.EtudiantService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;




import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;



import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


public class MainActivity extends AppCompatActivity {
    private EditText nom;
    private EditText prenom, date;
    private Button add;
    private ImageView imageViewSelectedPhoto;
    private Button btnSelectPhoto, btnTakePhoto;

    private EditText id;
    private Button rechercher, delete, list;
    private TextView res;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;
    private static final int PERMISSION_REQUEST_CAMERA = 100;
    private static final int PERMISSION_REQUEST_STORAGE = 101;

    private String currentPhotoPath;
    private byte[] photoData;

    void clear() {
        nom.setText("");
        prenom.setText("");
        date.setText("");
        imageViewSelectedPhoto.setImageResource(R.drawable.ic_person_placeholder);
        photoData = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final EtudiantService es = new EtudiantService(this);

        nom = (EditText) findViewById(R.id.nom);
        prenom = (EditText) findViewById(R.id.prenom);
        date = (EditText) findViewById(R.id.dateNaissance);
        add = (Button) findViewById(R.id.bn);
        Button datePickerButton = findViewById(R.id.datePickerButton);

        // Initialiser les contrôles pour la photo
        imageViewSelectedPhoto = findViewById(R.id.imageViewSelectedPhoto);
        btnSelectPhoto = findViewById(R.id.btnSelectPhoto);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);

        // Configurer les boutons pour les photos
        btnSelectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkStoragePermissionAndOpenGallery();
            }
        });

        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCameraPermissionAndTakePicture();
            }
        });

        // Code existant pour le DatePicker
        Calendar calendar = Calendar.getInstance();
        datePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                calendar.set(Calendar.YEAR, year);
                                calendar.set(Calendar.MONTH, month);
                                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
                                date.setText(sdf.format(calendar.getTime()));
                            }
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });

        // Mettre à jour le bouton d'ajout pour inclure la photo
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nomText = nom.getText().toString().trim();
                String prenomText = prenom.getText().toString().trim();
                String dateText = date.getText().toString().trim();

                if (nomText.isEmpty() || prenomText.isEmpty() || dateText.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                    return;
                }

                Etudiant nouvelEtudiant = new Etudiant(nomText, prenomText, dateText);

                // Ajouter la photo si disponible
                if (photoData != null) {
                    nouvelEtudiant.setPhoto(photoData);
                }

                es.create(nouvelEtudiant);
                Toast.makeText(MainActivity.this, "Étudiant ajouté avec succès", Toast.LENGTH_SHORT).show();
                clear();
            }
        });

        // Reste du code existant...
        id = (EditText) findViewById(R.id.id);
        rechercher = (Button) findViewById(R.id.load);
        delete = (Button) findViewById(R.id.delete);
        list = (Button) findViewById(R.id.list);
        res = (TextView) findViewById(R.id.res);

        rechercher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (id.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Veuillez entrer un ID", Toast.LENGTH_SHORT).show();
                    return;
                }

                Etudiant e = es.findById(Integer.parseInt(id.getText().toString()));

                if (e != null) {
                    res.setText(e.getNom()+" "+e.getPrenom());

                    // Afficher la photo si disponible
                    if (e.getPhoto() != null) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(e.getPhoto(), 0, e.getPhoto().length);
                        imageViewSelectedPhoto.setImageBitmap(bitmap);
                        photoData = e.getPhoto();
                        nom.setText(e.getNom().toString());
                        prenom.setText(e.getPrenom().toString());
                        date.setText(e.getDateNaissance().toString());

                    } else {
                        imageViewSelectedPhoto.setImageResource(R.drawable.ic_person_placeholder);
                        photoData = null;
                    }
                } else {
                    res.setText("Étudiant n'existe pas");
                }
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (id.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Veuillez entrer un ID", Toast.LENGTH_SHORT).show();
                    return;
                }

                Etudiant e = es.findById(Integer.parseInt(id.getText().toString()));
                if (e != null) {
                    es.delete(e);
                    res.setText("L'étudiant est supprimé");
                    clear();
                } else {
                    res.setText("Étudiant n'existe pas");
                }
            }
        });

        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), EtudiantActivity.class);
                startActivity(intent);
            }
        });
    }

    // Méthodes pour gérer les permissions et la sélection de photos
    private void checkCameraPermissionAndTakePicture() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
        } else {
            dispatchTakePictureIntent();
        }
    }

    private void checkStoragePermissionAndOpenGallery() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+) utilise READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        PERMISSION_REQUEST_STORAGE);
            } else {
                openGallery();
            }
        } else {
            // Versions antérieures d'Android
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_STORAGE);
            } else {
                openGallery();
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Créer le fichier où la photo sera enregistrée
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Erreur lors de la création du fichier image", Toast.LENGTH_SHORT).show();
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.sqliteapp.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Permission de caméra requise", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Permission de stockage requise", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // La photo a été prise et enregistrée à currentPhotoPath
                processAndSetCapturedImage();
            } else if (requestCode == REQUEST_IMAGE_GALLERY && data != null) {
                // La photo a été sélectionnée depuis la galerie
                try {
                    Uri selectedImageUri = data.getData();
                    InputStream imageStream = getContentResolver().openInputStream(selectedImageUri);
                    Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                    // Compresser l'image
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    selectedImage.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                    photoData = stream.toByteArray();

                    // Afficher l'image
                    imageViewSelectedPhoto.setImageBitmap(selectedImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Erreur lors du chargement de l'image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void processAndSetCapturedImage() {
        // Charger l'image depuis le chemin et la redimensionner
        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);

        // Vérifier si l'image est trop grande et la redimensionner si nécessaire
        Bitmap resizedBitmap = getResizedBitmap(bitmap, 800); // Taille max 800px

        // Convertir le bitmap en tableau de bytes pour la base de données
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        photoData = stream.toByteArray();

        // Afficher l'image
        imageViewSelectedPhoto.setImageBitmap(resizedBitmap);
    }

    private Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }
}