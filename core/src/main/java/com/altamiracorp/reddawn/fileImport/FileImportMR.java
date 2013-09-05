package com.altamiracorp.reddawn.fileImport;

import com.altamiracorp.reddawn.ConfigurableMapJobBase;
import com.altamiracorp.reddawn.RedDawnMapper;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRepository;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.ToolRunner;

import java.io.ByteArrayInputStream;

public class FileImportMR extends ConfigurableMapJobBase{

    @Override
    protected Class<? extends InputFormat> getInputFormatClassAndInit(Job job) {
        return WholeFileInputFormat.class;
    }

    @Override
    protected Class<? extends Mapper> getMapperClass(Job job, Class clazz) {
        return FileImportMapper.class;
    }

    public static class FileImportMapper extends RedDawnMapper<MapWritable, BytesWritable, Text, Artifact> {
        ArtifactRepository artifactRepository = new ArtifactRepository();

        @Override
        protected void safeMap(MapWritable metadata, BytesWritable value, Context context) throws Exception {
            long length = ((LongWritable)metadata.get(new Text("length"))).get();
            String name = metadata.get(new Text("name")).toString();
            long lastModified = ((LongWritable)metadata.get(new Text("lastModified"))).get();

            Artifact artifact = artifactRepository.createArtifactFromInputStream(getSession().getModelSession(),
                    length,
                    new ByteArrayInputStream(value.getBytes()),
                    name,
                    lastModified);
            artifactRepository.saveToGraph(getSession().getModelSession(), getSession().getGraphSession(), artifact);
            context.write(new Text(Artifact.TABLE_NAME),artifact);
        }
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new FileImportMR(), args);
        if (res != 0) {
            System.exit(res);
        }
    }

    @Override
    protected boolean hasConfigurableClassname() {
        return false;
    }
}
