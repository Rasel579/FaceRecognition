package org.diplom.vgg16;

import org.datavec.api.split.FileSplit;
import org.datavec.api.split.InputSplit;
import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.VGG16ImagePreProcessor;
import org.slf4j.Logger;
import org.slf4j.impl.StaticLoggerBinder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class VGG16Cat {
    private static final Logger LOGGER = StaticLoggerBinder.getSingleton().getLoggerFactory().getLogger(VGG16Cat.class.getName());
    private static final String TRAINED_PATH_MODEL = TrainImageNetVGG16.DATA_PATH + "/saved/modelIteration_1400_epoch_1.zip";
    private static ComputationGraph computationGraph;

    public static void main(String[] args) throws IOException {
        new VGG16Cat().runOnTestSet();
    }

    public PetType detectCat(File file, Double threshold) throws IOException {
        if (computationGraph == null) {
            computationGraph = loadModel();
        }

        computationGraph.init();

        LOGGER.error("VGG16Cat::detectCat " + computationGraph.summary());

        NativeImageLoader loader = new NativeImageLoader(224, 224, 3);
        INDArray image = loader.asMatrix(Files.newInputStream(file.toPath()));

        DataNormalization scale = new VGG16ImagePreProcessor();
        scale.transform(image);

        INDArray output = computationGraph.outputSingle(false, image);

        if (output.getDouble(0) > threshold) {
            return PetType.CAT;
        }

        if (output.getDouble(1) > threshold) {
            return PetType.DOG;
        }

        return PetType.NOT_KNOWN;
    }

    private void runOnTestSet() throws IOException {
        ComputationGraph computationGraph = loadModel();
        File trainData = new File(TrainImageNetVGG16.TEST_FOLDER);
        FileSplit test = new FileSplit(trainData, NativeImageLoader.ALLOWED_FORMATS, TrainImageNetVGG16.RANDOM_NUM_GEN);
        InputSplit inputSplit = test.sample(TrainImageNetVGG16.PATH_FILTER, 100, 0)[0];
        DataSetIterator dataSetIterator = TrainImageNetVGG16.getDataSetIterator(inputSplit);
        TrainImageNetVGG16.evalOn(computationGraph, dataSetIterator, 1);

    }

    public ComputationGraph loadModel()  {
        LOGGER.error("llloooad");
        try {
            computationGraph = ComputationGraph.load(new File(TRAINED_PATH_MODEL), false);
        }catch (IOException exception){
            LOGGER.error(exception.getMessage());
        }
        return computationGraph;
    }
}
