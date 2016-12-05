# phasing no filtering
module load Molgenis-Compute/v16.05.1-Java-1.8.0_45
sh ~/molgenis-compute-v16.05.1/molgenis_compute.sh \
  --backend slurm \
  --generate \
  -p samplesheetPhasing_test.csv \
  -w workflow.csv \
  -p parametersPhasing_test.converted.csv \
  -p chromosomes_X_Y.csv \
  -p chromosome_chunks.csv \
  -rundir jobs/ --weave 


