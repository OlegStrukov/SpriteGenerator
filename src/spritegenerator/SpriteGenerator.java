package spritegenerator;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.util.Collections;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javax.imageio.ImageIO;

/**
 *
 * @author Everol
 */
public class SpriteGenerator extends Application {

    int countImages = 0;
    int selectedCols = 0;
    int selectedRows = 0;
    int indexSelectColsRows;
    
    String selectsExpansion;
    String imageOnePath;
    
    //флаги для сборки спрайта
    boolean flagImages = false;
    boolean flagGrid = false;
    boolean flagExpansion = false;
    boolean flagCreate = false;
    
    Scene scene;
    Label labelCountImages = new Label("Количество изображений: 0");
    Label labelPreview = new Label("Preview");
    Label labelSettings = new Label("Настройки");
    Label labelColsRows = new Label("Кол-во столбцов и строк");
    Label labelSaveIn = new Label("Сохраняемое расширение");
    ArrayList<SelectColsRows> arrColsRows;
    ComboBox<SelectColsRows> selectColsRows;
    ObservableList<String> expansionOptions;
    ComboBox expansionSelect;
    Button buttonSaveSprite;
    FileChooser fileChooser;
    Button buttonSelectImages;
    Button buttonClearAll;
    Button reverseImages;
    List<File> imagesPath;
    List<Image> AllImages = new LinkedList();
    Image spriteImage;
    ImageView imageView[];
    TilePane imgBox;
   
    //для миниатюр. новая ширина и высота
    double conteinerImgWidth = 555;
    double minImgWidth = 0;
    double minImgHeight = 0;
    double ratioForHeight = 0;
    
    public static void main(String[] args) {
        Application.launch(args);
    }
    
    @Override
    public void start(Stage window) throws Exception {
        
        //список колонок строк
        arrColsRows = new ArrayList<>();
      
        //выбор изображений
        fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("JPG", "*.jpg"), 
            new FileChooser.ExtensionFilter("PNG", "*.png"));
         
        //кнопка добавления картинок
        buttonSelectImages = new Button("Выберите файлы");
        buttonSelectImages.setMinWidth(120);
        buttonSelectImages.setOnAction(e -> {
            selectImagesButton(window);
        });
        
        
        //селект выбора количества строк и колонок
        selectColsRows = new ComboBox<>();
        selectColsRows.setDisable(true);
        selectColsRows.setPromptText("Выберите изображения!");
        selectColsRows.setMinWidth(185);
        selectColsRows.setOnAction(e -> {
            if(!arrColsRows.isEmpty()) {
                indexSelectColsRows = selectColsRows.getSelectionModel().getSelectedIndex();
                selectedCols = arrColsRows.get(indexSelectColsRows).getCols();
                selectedRows = arrColsRows.get(indexSelectColsRows).getRows();                
                //перерисовываем превью в зависимости от количество строк и столбцов
                updateImagesView();
                checkCreateSprite();
            }            
        });
           
        //select вывода расширения файла
        expansionOptions = FXCollections.observableArrayList("jpg","png");
        expansionSelect = new ComboBox(expansionOptions);
        expansionSelect.setPromptText("Расширение файла");
        expansionSelect.setMinWidth(185);
        expansionSelect.setOnAction(e -> {
            if(expansionSelect.getSelectionModel().getSelectedIndex() != -1) {
                selectsExpansion = expansionSelect.getSelectionModel().getSelectedItem().toString();
                checkCreateSprite();
            }            
        });
        
        //кнопка сохранения спрайта
        buttonSaveSprite = new Button("Собрать спрайт");
        buttonSaveSprite.setDisable(true);
        buttonSaveSprite.setPadding(new Insets(10,35,10,35));        
        buttonSaveSprite.setOnAction(e -> {
            createSprite(window);
        });
                
        //кнопка очистить все
        buttonClearAll = new Button("Очистить");
        buttonClearAll.setMinWidth(120);       
        buttonClearAll.setOnAction(e -> {
            clearAll();
            checkCreateSprite();
        });
        
        //кнопка изменения направления массива
        reverseImages = new Button("Reverse");
        reverseImages.setMinWidth(120);
        reverseImages.setOnAction(e -> {
            Collections.reverse(AllImages);
            updateImagesView();
            checkCreateSprite();
        });
                                        
        GridPane root = BuildUI();
        scene = new Scene(root, 800, 500);
        
        //перетаскивание на сцену
        scene.setOnDragDropped(this::selectImagesDrag);
        scene.setOnDragOver(this::dragOver);

        window.setScene(scene);
        root.setStyle("-fx-padding: 10;"
                + "-fx-border-style: solid inside;"
                + "-fx-border-width: 2;"
                + "-fx-border-insets: 5;"
                + "-fx-border-radius: 5;"
                + "-fx-border-color: gray;");
        window.setTitle("Sprite Generator");
        window.setResizable(false);
        window.show();
    }
    
    //добавление нужных элементов в окно
    private GridPane BuildUI() {            
        //позиции элементов
        labelSettings.setAlignment(Pos.CENTER);
        labelSettings.setMinWidth(180.0);
        labelSettings.setPadding(new Insets(0, 0, 10, 0));
        labelSaveIn.setPadding(new Insets(10, 0, 0, 0));
        
        //врапер превьюх
        imgBox = new TilePane();
        imgBox.setHgap(0);
        imgBox.setVgap(0);
        ScrollPane imagesWrap = new ScrollPane(imgBox);
        imagesWrap.setPrefViewportHeight(400);
        imagesWrap.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        imagesWrap.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        imagesWrap.setFitToWidth(true);
        
        //лейбл превью и лейбл количество картинок
        HBox hBox1 = new HBox();
        hBox1.setSpacing(370.0);
        
        hBox1.setPadding(new Insets(0, 5, 10, 5));
        hBox1.getChildren().add(labelPreview);
        hBox1.getChildren().add(labelCountImages);
        
        VBox vBox1 = new VBox(hBox1,imagesWrap);
        
        //кнопки очистить, reverse, выбрать изображения
        HBox hBox2 = new HBox();
        hBox2.setSpacing(105.0);
        
        hBox2.setPadding(new Insets(10, 5, 0, 5));
        hBox2.getChildren().add(buttonSelectImages);
        hBox2.getChildren().add(reverseImages);        
        hBox2.getChildren().add(buttonClearAll);
        
        //вторая колонка
        VBox vBox2 = new VBox(labelSettings, labelColsRows, selectColsRows, labelSaveIn, expansionSelect);
        vBox2.setPadding(new Insets(0, 5, 0, 10));
        
        HBox hBox3 = new HBox();
        hBox3.setAlignment(Pos.CENTER);
        hBox3.setPadding(new Insets(5,0,0,0));
        hBox3.getChildren().add(buttonSaveSprite);

        GridPane box = new GridPane();
        
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(75);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(25);
        box.getColumnConstraints().addAll(col1,col2);
        
        box.add(vBox1, 0, 0);
        box.add(hBox2, 0, 1);
        box.add(vBox2, 1, 0);
        box.add(hBox3, 1, 1);
                        
        return box;
    }
    
    
    
    
    
    private void clearAll() {        
        //чистим массив изображений и превью
        AllImages.clear();
        imagesPath = null;
        imgBox.getChildren().clear();
        
         //чистим массив столбцов и строк
        arrColsRows.clear();
        indexSelectColsRows = 0;
        
        //количество стобцов/строк
        selectedCols = 0;
        selectedRows = 0;
                               
        //обнуляем селект выбора строк и столбцов
        selectColsRows.setDisable(true);
        selectColsRows.getSelectionModel().clearSelection();
        selectColsRows.getItems().clear();
                
        //сбрасываем селект сохраняемого расширения
        expansionSelect.valueProperty().set(null);
        selectsExpansion = null;
        
        //количество изображений луйбл
        labelCountImages.setText("Количество изображений: 0");
        
        //обнуляем количество изображений
        countImages = 0;
    }

    //добавление изображений перетаскиванием
    private void dragOver(DragEvent de) {
        Dragboard db = de.getDragboard();
        if (db.hasFiles() || db.hasImage()) {
            de.acceptTransferModes(TransferMode.ANY);
        }
        de.consume();
    }
 
    private void selectImagesDrag(DragEvent de) {
        clearAll();
        Dragboard db = de.getDragboard();
        imagesPath = db.getFiles();
        addImages();
    }
    
    //добавление изображений по кнопке
    private void selectImagesButton(Stage window) {
        clearAll();
        imagesPath = fileChooser.showOpenMultipleDialog(window);
        addImages();                        
    }

    private void addImages() {
        //показываем изображения
        fillImagesArray(imagesPath);

        //перебираем все изображения и формируем селект количества выбранных строк и столбцов
        int i = 1;
        double divisionResult = countImages/i;
        int currect = 0;
        String textSelect = "";

        while(i < divisionResult) {
            divisionResult = (double)countImages/i;
            if (divisionResult % 1 == 0) {
                currect = (int) divisionResult; 
                textSelect = "Столбцов: " + Integer.toString(i) + "; Строк: "+ Integer.toString(currect);
                arrColsRows.add(new SelectColsRows(i,currect, textSelect));
            }
            i++;
        }

        //после того как выбраны картинки довляем в селект новые элементы
        ObservableList<SelectColsRows> data = FXCollections.observableArrayList(arrColsRows);
        selectColsRows.setItems(data);
        selectColsRows.getSelectionModel().select(0);        
        checkCreateSprite();
    }

    private void fillImagesArray(List<File> imagesPath) {
        int count = 0;
        if (imagesPath == null || imagesPath.isEmpty()) {
            return;
        }
        for (File imagePath : imagesPath) {         
            try {
                imageOnePath = imagePath.toURI().toURL().toString();
            } catch (MalformedURLException ex) {
                clearAll();
            }
            AllImages.add(new Image(imageOnePath));
            count++;
        }
        countImages = count;
        
        //добавляем новое количество изображений в лейбл
        labelCountImages.setText("Количество изображений: " + countImages);
        
        //отрисовываем изображения
        updateImagesView();
    }
   
    private void updateImagesView() {       
        if(selectedCols != 0) {
            minImgWidth = conteinerImgWidth/selectedCols;    
            ratioForHeight = (100*minImgWidth)/(int) AllImages.get(0).getWidth();
            minImgHeight = (ratioForHeight*(int) AllImages.get(0).getHeight())/100;
        }            
        //чистим обзорщик
        imgBox.getChildren().clear();
        
        imageView = new ImageView[countImages];
        for (int j = 0; j < imageView.length; j++) {
            imageView[j] = new ImageView(AllImages.get(j));
            imageView[j].setFitHeight(minImgWidth);
            imageView[j].setFitWidth(minImgHeight);
            imageView[j].setSmooth(true);
            imageView[j].setPreserveRatio(true);
        }
        
        //добавляем массив изображений в обзорщик
        imgBox.getChildren().addAll(imageView);
    }

    private void checkCreateSprite() {
        //проверяем выбранны ли изображения
        if (imagesPath != null && !(imagesPath.isEmpty())) {
            flagImages = true;
            selectColsRows.setDisable(false);
        } else {
            flagImages = false;
        }

        //проверяем выбранна ли сетка сохранения
        if(selectedCols != 0 && selectedRows != 0) {
            flagGrid = true;
        } else {
            flagGrid = false;
        }

        //проверяем выбранно ли расширение сохранения файла
        if(selectsExpansion != null && !(selectsExpansion.isEmpty())) {
            flagExpansion = true;
        } else {
            flagExpansion = false;
        }

        if(flagImages == true && flagGrid == true && flagExpansion == true) {
            buttonSaveSprite.setDisable(false);
        } else {
            buttonSaveSprite.setDisable(true);
        }
    }

    private void createSprite(Stage window) {
        //ширина одной картинки
        int widthOneImage = (int) AllImages.get(0).getWidth();
        //высота одной картинки
        int heightOneImage = (int) AllImages.get(0).getHeight();

        //ширина и высота создаваемого спрайта
        int widthSprite = (int) widthOneImage*selectedCols;
        int heightSprite = (int) heightOneImage*selectedRows;
        
        int typeRgbColor = 0;
        if("jpg".equals(selectsExpansion)) {
            typeRgbColor = BufferedImage.TYPE_INT_RGB;
        } else if("png".equals(selectsExpansion)) {
            typeRgbColor = BufferedImage.TYPE_INT_ARGB;
        }
              
        //создаем заготовку изображения
        BufferedImage combined = new BufferedImage(widthSprite, heightSprite, typeRgbColor);
        Graphics g = combined.getGraphics();
        
        //если jpg - закрашиваем белым, иначе изображение будет черным 
        if("jpg".equals(selectsExpansion)) {
            g.setColor(java.awt.Color.WHITE);
            g.fillRect( 0, 0, widthSprite, heightSprite);
        }
                     
        //сдвиг сверху и слева
        int leftStep = 0;
        int topStep = 0;
        
        //идем сначала по строкам, во внутреннем по колонкам
        int thisImage = -1;
        for (int i = 0 ; i < selectedRows; i++) {
            for (int j = 0 ; j < selectedCols; j++) {
                thisImage++;
                if(j == 0) {
                    leftStep = 0;
                }
                BufferedImage bImage = SwingFXUtils.fromFXImage(AllImages.get(thisImage), null);
                g.drawImage(bImage, leftStep, topStep, null);

                leftStep = leftStep + widthOneImage;
            }
            topStep = topStep + heightOneImage;
        }
        FileChooser fileChooserOne = new FileChooser();
        FileChooser.ExtensionFilter extFilter = null;
        if("jpg".equals(selectsExpansion)) {
            extFilter = new FileChooser.ExtensionFilter("jpg files (*.jpg)", "*.jpg");
        } else if("png".equals(selectsExpansion)) {
            extFilter = new FileChooser.ExtensionFilter("png files (*.png)", "*.png");
        }
        fileChooserOne.getExtensionFilters().add(extFilter);
        File file = fileChooserOne.showSaveDialog(window);
        if(file != null){
            try {
                buttonSaveSprite.setDisable(true);   
                if("jpg".equals(selectsExpansion)) {    
                    ImageIO.write(combined, "jpg", file);
                } else if("png".equals(selectsExpansion)) {                   
                    ImageIO.write(combined, "png", file);
                }    
                //удаляем после сохранения
                buttonSaveSprite.setDisable(false);
                combined = null;
                g = null;
                showAlertSave();                
            } catch (IOException ex) {
                Logger.getLogger(SpriteGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }

    //Спрайт сохранен
    private void showAlertSave() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Сохранено");
        alert.setHeaderText(null);
        alert.setContentText("Спрайт успешно сохранен!");
        alert.getButtonTypes().setAll(ButtonType.OK);
        alert.showAndWait();
    }
    
}
