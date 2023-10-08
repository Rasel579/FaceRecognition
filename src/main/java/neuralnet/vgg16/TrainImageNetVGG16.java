package neuralnet.vgg16;

import org.apache.ant.compress.taskdefs.Unzip;
import org.apache.commons.io.FileUtils;
import org.datavec.api.io.filters.BalancedPathFilter;
import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.split.FileSplit;
import org.datavec.api.split.InputSplit;
import org.datavec.image.loader.BaseImageLoader;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.ImageRecordReader;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.transferlearning.FineTuneConfiguration;
import org.deeplearning4j.nn.transferlearning.TransferLearning;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.deeplearning4j.zoo.PretrainedType;
import org.deeplearning4j.zoo.ZooModel;
import org.deeplearning4j.zoo.model.VGG16;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.VGG16ImagePreProcessor;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.zip.Adler32;


public class TrainImageNetVGG16 {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrainImageNetVGG16.class);
    private static final long seed = 12345;
    public static final Random RANDOM_NUM_GEN = new Random(seed);
    private static final String[] ALLOWED_FORMATS = BaseImageLoader.ALLOWED_FORMATS;
    private static final ParentPathLabelGenerator LABEL_GENERATOR_MAKER = new ParentPathLabelGenerator();
    public static final BalancedPathFilter PATH_FILTER = new BalancedPathFilter(RANDOM_NUM_GEN, ALLOWED_FORMATS, LABEL_GENERATOR_MAKER);

    private static final int EPOCH = 5;
    private static final int BATCH_SIZE = 16;
    private static final int TRAIN_SIZE = 85;
    private static final int NUM_POSSIBLE_LABELS = 2;

    private static final int SAVING_INTERVAL = 100;

    public static final String DATA_PATH = "resources";
    private static final String TRAIN_FOLDER = DATA_PATH + "/train_both";
    public static final String TEST_FOLDER = DATA_PATH + "/test_both";
    private static final String SAVING_PATH = DATA_PATH + "/saved/modelIteration_";

    private static final String FREEZE_UNTIL_LAYER = "fc2";

    private static final String DATA_URL = "https://dl.dropboxusercontent.com/s/tqnp49apphpzb40/dataTraining.zip?dl=0";

    private static void unzip(File fileZip) {
        Unzip unZiper = new Unzip();
        unZiper.setSrc(fileZip);
        unZiper.setDest(new File(DATA_PATH));
        unZiper.execute();
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        ZooModel zooModel = VGG16.builder().build();
        LOGGER.info("TrainImageNetVGG16::Start Downloading VGG16 model...");
        MultiLayerNetwork preTrainedNet = (MultiLayerNetwork) zooModel.initPretrained(PretrainedType.IMAGENET);
        LOGGER.info("TrainImageNetVGG16:: " + preTrainedNet.summary());

        downloadAndUnzipDataForFirstTime();

        File trainData = new File(TRAIN_FOLDER);
        File testData = new File(TEST_FOLDER);
        FileSplit train = new FileSplit(trainData, NativeImageLoader.ALLOWED_FORMATS, RANDOM_NUM_GEN);
        FileSplit test = new FileSplit(testData, NativeImageLoader.ALLOWED_FORMATS, RANDOM_NUM_GEN);

        InputSplit[] sample = train.sample(PATH_FILTER, TRAIN_SIZE, 100 - TRAIN_SIZE);

        DataSetIterator trainIterator = getDataSetIterator(sample[0]);
        DataSetIterator devIterator = getDataSetIterator(sample[1]);

        FineTuneConfiguration fineTuneConfiguration = new FineTuneConfiguration.Builder()
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(Updater.NESTEROVS)
                .seed(seed)
                .build();
        ComputationGraph vgg16Transfer = new TransferLearning.Builder(preTrainedNet)
                .fineTuneConfiguration(fineTuneConfiguration)
                .setFeatureExtractor(1)
                .addLayer(new OutputLayer.Builder(LossFunctions.LossFunction.SQUARED_LOSS)
                        .nIn(4096)
                        .nOut(NUM_POSSIBLE_LABELS)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.SOFTMAX)
                        .build())
                .build().toComputationGraph();

        vgg16Transfer.setListeners(new ScoreIterationListener(5));
        LOGGER.info("TrainImageNetVGG16:: summary" + vgg16Transfer.summary());

        DataSetIterator testIterator = getDataSetIterator(test.sample(PATH_FILTER, 1, 0)[0]);

        int indexEpoch = 0;
        int i = 0;
        while (indexEpoch < EPOCH) {
            while (trainIterator.hasNext()) {
                DataSet trained = trainIterator.next();
                vgg16Transfer.fit(trained);
                if (i % SAVING_INTERVAL == 0 && i != 0) {
                    ModelSerializer.writeModel(vgg16Transfer, new File(SAVING_PATH + i + "_epoch_" + indexEpoch + ".zip"), false);
                    evalOn(vgg16Transfer, devIterator, i);
                }
                i++;
            }
            trainIterator.reset();
            indexEpoch++;

            evalOn(vgg16Transfer, testIterator, indexEpoch);
        }

    }

    private static void downloadAndUnzipDataForFirstTime() throws IOException, URISyntaxException {
        File data = new File(DATA_PATH + "/data.zip");
        if (!data.exists() || FileUtils.checksum(data, new Adler32()).getValue() != 1195241806) {
            data.delete();
            FileUtils.copyURLToFile(new URI(DATA_URL).toURL(), data);
            LOGGER.info("TrainImageNetVGG16::downloadAndUnzipDataForFirstTime: File downloaded");
        }
        if (!new File(TRAIN_FOLDER).exists()) {
            LOGGER.info("TrainImageNetVGG16::downloadAndUnzipDataForFirstTime: unziping Data");
            unzip(data);
        }
    }

    public static DataSetIterator getDataSetIterator(InputSplit sample) throws IOException {
        ImageRecordReader imageRecordReader = new ImageRecordReader(224, 224, 3, LABEL_GENERATOR_MAKER);
        imageRecordReader.initialize(sample);

        DataSetIterator iterator = new RecordReaderDataSetIterator(imageRecordReader, BATCH_SIZE, 1, NUM_POSSIBLE_LABELS);
        iterator.setPreProcessor(new VGG16ImagePreProcessor());
        return iterator;
    }

    public static void evalOn(ComputationGraph vgg16Transfer, DataSetIterator testIterator, int indexEpoch) {
        LOGGER.info("TrainImageNetVGG16::evalOn Evaluate model at iteration " + indexEpoch + " ....");
        Evaluation eval = vgg16Transfer.evaluate(testIterator);
        LOGGER.info("TrainImageNetVGG16::evalOn " + eval.stats());
        testIterator.reset();
    }
}
