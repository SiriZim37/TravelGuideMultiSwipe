package com.siri.travelguidemultiswipedbrealtime.datasource

import com.siri.travelguidemultiswipedbrealtime.data.model.PlaceModel
import com.siri.travelguidemultiswipedbrealtime.datasource.base.BaseRepositoryManagement
import java.util.*

class PlaceTypeRepository : BaseRepositoryManagement<PlaceModel>() {

    override fun generateNewItem( placeType : String
                                  , placeName : String
                                  , review : String
                                  ,  imgUrl : String): PlaceModel {


        val place = PlaceModel( type = placeType,  name = placeName , review = review , imgLink = imgUrl )
        addItem(place)

        return place
    }


    private fun ClosedRange<Int>.random() =
        Random().nextInt((endInclusive + 1) - start) + start

    companion object {
        private var instance: PlaceTypeRepository? = null

        fun getInstance(): PlaceTypeRepository {
            if (instance == null)
                instance =
                    PlaceTypeRepository()

            return instance as PlaceTypeRepository
        }
    }
}