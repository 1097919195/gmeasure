//package com.npclo.imeasurer.data.measure;
//
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.annotation.VisibleForTesting;
//
//import com.npclo.imeasurer.data.measure.local.MeasurementLocalDataSource;
//
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.NoSuchElementException;
//
//import rx.Observable;
//import rx.functions.Func1;
//
//import com.npclo.imeasurer.data.measure.remote.MeasurementRemoteDataSource;
//
//import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;
//
///**
// * Created by Endless on 2017/7/31.
// */
//
//public class MeasurementRepository implements MeasurementDataSource {
//    @Nullable
//    private static MeasurementRepository INSTANCE = null;
//    @Nullable
//    private static MeasurementRemoteDataSource measurementRemoteDataSource;
//    @VisibleForTesting
//    private boolean mCacheIsDirty = false;
//
//    @VisibleForTesting
//    @Nullable
//    private Map<String, Measurement> mCachedMeasurement;
//
//    private MeasurementRepository(@NonNull MeasurementRemoteDataSource remoteDataSource) {
//        measurementRemoteDataSource = remoteDataSource;
////        measurementLocalDataSource = localDataSource;
//    }
//
//    public static MeasurementRepository getInstance(@NonNull MeasurementRemoteDataSource remoteDataSource) {
//        if (INSTANCE == null) {
//            INSTANCE = new MeasurementRepository(remoteDataSource);
//        }
//        return INSTANCE;
//    }
//
//    public static void destroyInstance() {
//        INSTANCE = null;
//    }
//
//    @Override
//    public Observable<List<Measurement>> getMeasurements() {
//        // Respond immediately with cache if available and not dirty
//        if (mCachedMeasurement != null && !mCacheIsDirty) {
//            return Observable.from(mCachedMeasurement.values()).toList();
//        } else if (mCachedMeasurement == null) {
//            mCachedMeasurement = new LinkedHashMap<>();
//        }
//
//        Observable<List<Measurement>> remoteMeasurements = getAndSaveRemoteMeasurements();
//
//        if (mCacheIsDirty) {
//            return remoteMeasurements;
//        } else {
//            // Query the local storage if available. If not, query the network.
//            Observable<List<Measurement>> localMeasurements = getAndSaveRemoteMeasurements();
//            return Observable.concat(localMeasurements, remoteMeasurements)
//                    .filter(Measurements -> !Measurements.isEmpty())
//                    .first();
//        }
//    }
//
//    @Override
//    public Observable<Measurement> getMeasurement(@NonNull String id) {
//        checkNotNull(id);
//
//        final Measurement cachedMeasurement = getMeasurementWithId(id);
//
//        // Respond immediately with cache if available
//        if (cachedMeasurement != null) {
//            return Observable.just(cachedMeasurement);
//        }
//
//        // Load from server/persisted if needed.
//
//        // Do in memory cache update to keep the app UI up to date
//        if (mCachedMeasurement == null) {
//            mCachedMeasurement = new LinkedHashMap<>();
//        }
//
//        // Is the measurement in the local data source? If not, query the network.
//        Observable<Measurement> remoteMeasurement = measurementRemoteDataSource
//                .getMeasurement(id)
//                .doOnNext(m -> {
//                    mCachedMeasurement.put(m.get_id(), m);
//                });
//
//        return Observable.concat(localMeasurement, remoteMeasurement).first()
//                .map(measurement -> {
//                    if (measurement == null) {
//                        throw new NoSuchElementException("No measurement found with id " + id);
//                    }
//                    return measurement;
//                });
//    }
//
//    @Override
//    public void saveMeasurement(@NonNull Measurement measurement) {
//        checkNotNull(measurement);
//        measurementRemoteDataSource.saveMeasurement(measurement);
//
//        // Do in memory cache update to keep the app UI up to date
//        if (mCachedMeasurement == null) {
//            mCachedMeasurement = new LinkedHashMap<>();
//        }
//        mCachedMeasurement.put(measurement.get_id(), measurement);
//    }
//
//    @Override
//    public void refreshMeasurements() {
//        mCacheIsDirty = true;
//    }
//
//    private Observable<List<Measurement>> getAndSaveRemoteMeasurements() {
//        return measurementRemoteDataSource.getMeasurements()
//                .flatMap(new Func1<List<Measurement>, Observable<List<Measurement>>>() {
//                    @Override
//                    public Observable<List<Measurement>> call(List<Measurement> Measurements) {
//                        return Observable.from(Measurements).doOnNext(measurement -> {
//                            mCachedMeasurement.put(measurement.get_id(), measurement);
//                        }).toList();
//                    }
//                })
//                .doOnCompleted(() -> mCacheIsDirty = false);
//    }
//
//    @Nullable
//    private Measurement getMeasurementWithId(@NonNull String id) {
//        checkNotNull(id);
//        if (mCachedMeasurement == null || mCachedMeasurement.isEmpty()) {
//            return null;
//        } else {
//            return mCachedMeasurement.get(id);
//        }
//    }
//}
