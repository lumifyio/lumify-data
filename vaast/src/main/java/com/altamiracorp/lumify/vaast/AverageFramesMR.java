package com.altamiracorp.lumify.vaast;

import com.altamiracorp.lumify.ConfigurableMapJobBase;
import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.AccumuloModelOutputFormat;
import com.altamiracorp.lumify.model.AccumuloSession;
import com.altamiracorp.lumify.model.AccumuloVideoFrameInputFormat;
import com.altamiracorp.lumify.model.TitanGraphSession;
import com.altamiracorp.lumify.core.model.videoFrames.VideoFrame;
import com.altamiracorp.lumify.core.model.videoFrames.VideoFrameRepository;
import com.altamiracorp.lumify.core.model.videoFrames.VideoFrameRowKey;
import com.altamiracorp.lumify.search.BlurSearchProvider;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.vaast.model.averageFrames.AverageFrame;
import com.altamiracorp.lumify.vaast.model.averageFrames.AverageFrameRepository;
import com.altamiracorp.lumify.vaast.model.averageFrames.AverageFrameRowKey;
import com.altamiracorp.vaast.core.model.AveragedFrame;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.commons.cli.CommandLine;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.util.ToolRunner;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

public class AverageFramesMR extends ConfigurableMapJobBase {

    @Override
    public int run(CommandLine cmd) throws Exception {
        Job job = new Job(getConf(), this.getClass().getSimpleName());
        job.getConfiguration().set(AccumuloSession.HADOOP_URL, getHadoopUrl());
        job.getConfiguration().set(AccumuloSession.ZOOKEEPER_INSTANCE_NAME, getZookeeperInstanceName());
        job.getConfiguration().set(AccumuloSession.ZOOKEEPER_SERVER_NAMES, getZookeeperServerNames());
        job.getConfiguration().set(AccumuloSession.USERNAME, getUsername());
        job.getConfiguration().set(AccumuloSession.PASSWORD, new String(getPassword()));
        if (getBlurControllerLocation() != null) {
            job.getConfiguration().set(BlurSearchProvider.BLUR_CONTROLLER_LOCATION, getBlurControllerLocation());
        }
        if (getBlurHdfsPath() != null) {
            job.getConfiguration().set(BlurSearchProvider.BLUR_PATH, getBlurHdfsPath());
        }
        if (getGraphStorageIndexSearchHostname() != null) {
            job.getConfiguration().set(TitanGraphSession.STORAGE_INDEX_SEARCH_HOSTNAME, getGraphStorageIndexSearchHostname());
        }
        job.setJarByClass(this.getClass());


        if (getConfig() != null) {
            for (String config : getConfig()) {
                String[] parts = config.split("=", 2);
                job.getConfiguration().set(parts[0], parts[1]);
            }
        }

        job.setInputFormatClass(getInputFormatClassAndInit(job));

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(MapWritable.class);
        job.setMapperClass(getMapperClass(job, getClazz()));
        job.setReducerClass(AverageFramesReducer.class);

        job.setNumReduceTasks(1);

        Class<? extends OutputFormat> outputFormatClass = getOutputFormatClass();
        if (outputFormatClass != null) {
            job.setOutputFormatClass(outputFormatClass);
        }
        AccumuloModelOutputFormat.init(job, getUsername(), getPassword(), getZookeeperInstanceName(), getZookeeperServerNames(), Artifact.TABLE_NAME);

        job.waitForCompletion(true);
        return job.isSuccessful() ? 0 : 1;
    }

    @Override
    protected Class<? extends InputFormat> getInputFormatClassAndInit(Job job) {
        AccumuloVideoFrameInputFormat.init(job, getUsername(), getPassword(), getAuthorizations(), getZookeeperInstanceName(), getZookeeperServerNames());
        return AccumuloVideoFrameInputFormat.class;
    }

    @Override
    protected Class<? extends Mapper> getMapperClass(Job job, Class clazz) {
        return AverageFramesMapper.class;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public static class AverageFramesMapper extends Mapper<Text, VideoFrame, Text, MapWritable> {
        private AppSession session;
        private VideoFrameRepository videoFrameRepository = new VideoFrameRepository();

        private int framesPerAverage;
        private int frameDelta;

        private static final String FRAMES_PER_AVERAGE = "framesPerAverage";
        private static final String FRAME_DELTA = "frameDelta";

        private static final String DEFAULT_FRAMES_PER_AVERAGE = "10";
        private static final String DEFAULT_FRAME_DELTA = "200";

        @Override
        public void setup(Context context) {
            session = createAppSession(context);
            framesPerAverage = Integer.parseInt(context.getConfiguration().get(FRAMES_PER_AVERAGE, DEFAULT_FRAMES_PER_AVERAGE));
            frameDelta = Integer.parseInt(context.getConfiguration().get(FRAME_DELTA, DEFAULT_FRAME_DELTA));
        }

        @Override
        public void map(Text rowKey, VideoFrame vFrame, Context context) throws IOException {
            try {
                VideoFrameRowKey vFrameRowKey = new VideoFrameRowKey(rowKey.toString());
                long time = vFrameRowKey.getTime();
                int groupNumber = (int) Math.floor((time / frameDelta) / framesPerAverage);

                //let's read the image and convert it to grayscale
                BufferedImage bImage = videoFrameRepository.loadImage(session.getModelSession(), vFrame);
                BufferedImage grayImage = new BufferedImage(bImage.getWidth(), bImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
                new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY),null).filter(bImage,grayImage);

                //TODO: Better way to do this without writing our own writable??
                MapWritable vMap = new MapWritable();
                vMap.put(new Text("width"),new BytesWritable(ByteBuffer.allocate(4).putInt(grayImage.getWidth()).array()));
                vMap.put(new Text("height"),new BytesWritable(ByteBuffer.allocate(4).putInt(grayImage.getHeight()).array()));
                vMap.put(new Text("channels"), new BytesWritable(ByteBuffer.allocate(4).putInt(1).array())); // 1 channel for grayscale
                vMap.put(new Text("data"),new BytesWritable(((DataBufferByte) grayImage.getRaster().getDataBuffer()).getData()));

                context.write(new Text(new AverageFrameRowKey(vFrameRowKey.getArtifactRowKey().toString(),groupNumber).toString()), vMap);
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

    }

    public static class AverageFramesReducer extends Reducer<Text, MapWritable, Text, AverageFrame> {
        private AppSession session;
        private AverageFrameRepository averageFrameRepository = new AverageFrameRepository();

        @Override
        public void setup(Reducer.Context context) {
            session = createAppSession(context);
            session.getModelSession().initializeTable(AverageFrame.TABLE_NAME);
        }

        @Override
        public void reduce(Text frameGroupKey, Iterable<MapWritable> frames, Context context) throws IOException {
            try {
                Iterator<MapWritable> iter = frames.iterator();
                MapWritable currentFrame = iter.next();

                AveragedFrame aFrame = initAveragedFrame(currentFrame);

                //get the number of channels for later
                int channels = getChannels(currentFrame);

                //add the first frame
                aFrame.addFrame(0,getData(currentFrame));

                //now add the rest of the frames
                long i = 1;
                while (iter.hasNext()) {
                    currentFrame = iter.next();
                    aFrame.addFrame(i,getData(currentFrame));
                    i++;
                }

                byte[] averageBytes = aFrame.getAveragedBytes();
                AverageFrame averageFrame = averageFrameRepository.saveAverageFrame(session.getModelSession(), frameGroupKey.toString(), new ByteArrayInputStream(averageBytes));

                //set all the metadata
                averageFrame.getMetadata().setWidth(aFrame.getWidth());
                averageFrame.getMetadata().setHeight(aFrame.getHeight());
                averageFrame.getMetadata().setChannels(channels);

                context.write(new Text(AverageFrame.TABLE_NAME), averageFrame);
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        private AveragedFrame initAveragedFrame (MapWritable sampleFrameData) {
            //intialize the average frame with the data from the sample frame
            int width = getWidth(sampleFrameData);
            int height = getHeight(sampleFrameData);

            AveragedFrame aFrame = new AveragedFrame(width,height);
            return aFrame;
        }

        private byte[] getData (MapWritable map) {
            BytesWritable bw = (BytesWritable) map.get(new Text("data"));
            return bw.getBytes();
        }

        private int getHeight (MapWritable map) {
            return getIntegerValue(map,"height");
        }

        private int getWidth (MapWritable map) {
            return getIntegerValue(map, "width");
        }

        private int getChannels (MapWritable map) {
            return getIntegerValue(map, "channels");
        }

        private int getIntegerValue (MapWritable map, String key) {
            BytesWritable bw = (BytesWritable) map.get(new Text(key));
            return ByteBuffer.wrap(bw.getBytes()).getInt();
        }

    }

    @Override
    public boolean hasConfigurableClassname() {
        return false;
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new AverageFramesMR(), args);
        if (res != 0) {
            System.exit(res);
        }
    }
}
