package vi.filepicker;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.OnClick;
import java.util.ArrayList;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import droidninja.filepicker.models.sort.SortingTypes;
import droidninja.filepicker.utils.MediaPlayerManager;
import droidninja.filepicker.utils.Orientation;
import java.util.List;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

  public static final int RC_PHOTO_PICKER_PERM = 123;
  public static final int RC_FILE_PICKER_PERM = 321;
  public static final int RC_AUDIO_PICKER_PERM = 231;
  private static final int CUSTOM_REQUEST_CODE = 532;
  private int MAX_ATTACHMENT_COUNT = 10;
  private ArrayList<String> photoPaths = new ArrayList<>();
  private ArrayList<String> docPaths = new ArrayList<>();
  private ArrayList<String> audioPaths = new ArrayList<>();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    findViewById(R.id.pick_photo).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        pickPhotoClicked();
      }
    });
    findViewById(R.id.pick_doc).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        pickDocClicked();
      }
    });
    findViewById(R.id.pick_audio).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        pickAudioClicked();
      }
    });
  }

  @AfterPermissionGranted(RC_PHOTO_PICKER_PERM)
  public void pickPhotoClicked() {
    if (EasyPermissions.hasPermissions(this, FilePickerConst.PERMISSIONS_FILE_PICKER)) {
      onPickPhoto();
    } else {
      // Ask for one permission
      EasyPermissions.requestPermissions(this, getString(R.string.rationale_photo_picker),
          RC_PHOTO_PICKER_PERM, FilePickerConst.PERMISSIONS_FILE_PICKER);
    }
  }

  @AfterPermissionGranted(RC_FILE_PICKER_PERM)
  public void pickDocClicked() {
    if (EasyPermissions.hasPermissions(this, FilePickerConst.PERMISSIONS_FILE_PICKER)) {
      onPickDoc();
    } else {
      // Ask for one permission
      EasyPermissions.requestPermissions(this, getString(R.string.rationale_doc_picker),
          RC_FILE_PICKER_PERM, FilePickerConst.PERMISSIONS_FILE_PICKER);
    }
  }

  @AfterPermissionGranted(RC_AUDIO_PICKER_PERM)
  public void pickAudioClicked() {
    if (EasyPermissions.hasPermissions(this, FilePickerConst.PERMISSIONS_FILE_PICKER)) {
      onPickAudio();
    } else {
      // Ask for one permission
      EasyPermissions.requestPermissions(this, getString(R.string.rationale_audio_picker),
              RC_AUDIO_PICKER_PERM, FilePickerConst.PERMISSIONS_FILE_PICKER);
    }
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode) {
      case CUSTOM_REQUEST_CODE:
        if (resultCode == Activity.RESULT_OK && data != null) {
          photoPaths = new ArrayList<>();
          photoPaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA));
        }
        break;

      case FilePickerConst.REQUEST_CODE_AUDIO:
        MediaPlayerManager.getInstance().stop();

        if (resultCode == Activity.RESULT_OK && data != null) {
          audioPaths = new ArrayList<>();
          audioPaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_AUDIO));
        }
        break;

      case FilePickerConst.REQUEST_CODE_DOC:
        if (resultCode == Activity.RESULT_OK && data != null) {
          docPaths = new ArrayList<>();
          docPaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS));
        }
        break;
    }

    addThemToView(photoPaths, docPaths , audioPaths);
  }

  private void addThemToView(ArrayList<String> imagePaths, ArrayList<String> docPaths,ArrayList<String> audioPaths) {
    ArrayList<String> filePaths = new ArrayList<>();

    if (imagePaths != null) filePaths.addAll(imagePaths);

    if (docPaths != null) filePaths.addAll(docPaths);
    if (audioPaths != null) filePaths.addAll(audioPaths);

    RecyclerView recyclerView = findViewById(R.id.recyclerview);
    if (recyclerView != null) {
      StaggeredGridLayoutManager layoutManager =
          new StaggeredGridLayoutManager(3, OrientationHelper.VERTICAL);
      layoutManager.setGapStrategy(
          StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
      recyclerView.setLayoutManager(layoutManager);

      ImageAdapter imageAdapter = new ImageAdapter(this, filePaths);

      recyclerView.setAdapter(imageAdapter);
      recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    Toast.makeText(this, "Num of files selected: " + filePaths.size(), Toast.LENGTH_SHORT).show();
  }

  public void onPickPhoto() {
    int maxCount = MAX_ATTACHMENT_COUNT - docPaths.size();
    if ((docPaths.size() + photoPaths.size()) == MAX_ATTACHMENT_COUNT) {
      Toast.makeText(this, "Cannot select more than " + MAX_ATTACHMENT_COUNT + " items",
          Toast.LENGTH_SHORT).show();
    } else {
      FilePickerBuilder.Companion.getInstance()
          .setMaxCount(maxCount)
          .setSelectedFiles(photoPaths)
          .setActivityTheme(R.style.FilePickerTheme)
          .setActivityTitle("Please select media")
          .enableVideoPicker(true)
          .enableCameraSupport(true)
          .showGifs(true)
          .showFolderView(true)
          .enableSelectAll(false)
          .enableImagePicker(true)
          .setCameraPlaceholder(R.drawable.custom_camera)
          .withOrientation(Orientation.UNSPECIFIED)
          .pickPhoto(this, CUSTOM_REQUEST_CODE);
    }
  }

  public void onPickDoc() {
    String[] zips = { ".zip", ".rar" };
    String[] pdfs = { ".pdf" };
    int maxCount = MAX_ATTACHMENT_COUNT - photoPaths.size();
    if ((docPaths.size() + photoPaths.size()) == MAX_ATTACHMENT_COUNT) {
      Toast.makeText(this, "Cannot select more than " + MAX_ATTACHMENT_COUNT + " items",
          Toast.LENGTH_SHORT).show();
    } else {
      FilePickerBuilder.Companion.getInstance()
          .setMaxCount(maxCount)
          .setSelectedFiles(docPaths)
          .setActivityTheme(R.style.FilePickerTheme)
          .setActivityTitle("Please select doc")
          .addFileSupport("ZIP", zips)
          .addFileSupport("PDF", pdfs, R.drawable.pdf_blue)
          .enableDocSupport(false)
          .enableSelectAll(true)
          .sortDocumentsBy(SortingTypes.name)
          .withOrientation(Orientation.UNSPECIFIED)
          .pickFile(this);
    }
  }

  public void onPickAudio() {
    String[] audio = { ".mp3","m4a",".ogg" };
    if ((docPaths.size() + photoPaths.size()) + audioPaths.size() == MAX_ATTACHMENT_COUNT) {
      Toast.makeText(this, "Cannot select more than " + MAX_ATTACHMENT_COUNT + " items",
              Toast.LENGTH_SHORT).show();
    } else {
      FilePickerBuilder.Companion.getInstance()
              .setMaxCount(5)
              .enableSelectAll(false)
              .setSelectedFiles(audioPaths)
              .setActivityTheme(R.style.FilePickerTheme)
              .setActivityTitle("Pilih audio")
              .enableDocSupport(false)
              .addFileSupport("Audio", audio)
              .sortDocumentsBy(SortingTypes.name)
              .pickAudio(this);
    }
  }

  @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
  }

  public void onOpenFragmentClicked(View view) {
    Intent intent = new Intent(this, FragmentActivity.class);
    startActivity(intent);
  }

  @Override public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
  }

  @Override public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
      new AppSettingsDialog.Builder(this).build().show();
    }
  }
}
