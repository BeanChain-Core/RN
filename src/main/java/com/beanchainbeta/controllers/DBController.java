package com.beanchainbeta.controllers;

import org.springframework.web.bind.annotation.*;
import com.beanchainbeta.services.RewardDB;



@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/drip")
public class DBController {


    public DBController(RewardDB db){
        
    }

}
